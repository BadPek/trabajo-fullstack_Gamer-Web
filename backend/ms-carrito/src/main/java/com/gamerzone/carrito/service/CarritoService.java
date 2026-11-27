package com.gamerzone.carrito.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.gamerzone.carrito.dto.request.AplicarCuponRequest;
import com.gamerzone.carrito.dto.request.AgregarProductoRequest;
import com.gamerzone.carrito.dto.request.ActualizarCantidadRequest;
import com.gamerzone.carrito.dto.response.CarritoResponse;
import com.gamerzone.carrito.dto.response.CartCouponResponse;
import com.gamerzone.carrito.dto.response.CouponCalculationResponse;
import com.gamerzone.carrito.dto.response.ItemCarritoResponse;
import com.gamerzone.carrito.entity.Cart;
import com.gamerzone.carrito.entity.CartItem;
import com.gamerzone.carrito.entity.Coupon;
import com.gamerzone.carrito.integration.CatalogoClient;
import com.gamerzone.carrito.integration.CatalogoProductResponse;
import com.gamerzone.carrito.repository.CartRepository;
import com.gamerzone.carrito.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CarritoService {

    private static final int CANTIDAD_MAXIMA = 99;
    private static final int LINEAS_MAXIMAS = 50;
    private static final int UNIDADES_MAXIMAS = 500;

    private final CartRepository cartRepository;
    private final CatalogoClient catalogoClient;
    private final CouponRepository couponRepository;

    @Autowired
    public CarritoService(CartRepository cartRepository, CatalogoClient catalogoClient,
            CouponRepository couponRepository) {
        this.cartRepository = cartRepository;
        this.catalogoClient = catalogoClient;
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public CarritoResponse findCart(String customerId) {
        String normalizedUserId = normalizeUuid(customerId);
        return cartRepository.findFirstByUserIdAndStatus(normalizedUserId, Cart.STATUS_ACTIVE)
                .map(this::toResponse)
                .orElseGet(CarritoService::emptyCartResponse);
    }

    @Transactional
    public ItemCarritoResponse addItem(String customerId, AgregarProductoRequest request) {
        Cart cart = findOrCreateCart(customerId);
        String productId = normalizeUuid(request.productId());
        CatalogoProductResponse product = catalogoClient.findProduct(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "ms-catalogo no devolvió el producto solicitado.");
        }

        CartItem existing = cart.getItems().stream()
                .filter(item -> productId.equals(item.getProductId()))
                .findFirst()
                .orElse(null);

        int newQuantity = existing == null
                ? request.quantity()
                : existing.getQuantity() + request.quantity();
        if (newQuantity > CANTIDAD_MAXIMA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad máxima por producto es 99.");
        }
        if (existing == null && cart.getItems().size() >= LINEAS_MAXIMAS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El carrito admite como máximo 50 productos diferentes.");
        }
        if (totalUnits(cart, existing, newQuantity) > UNIDADES_MAXIMAS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El carrito admite como máximo 500 unidades.");
        }

        if (existing == null) {
            existing = new CartItem(productId, request.quantity());
            cart.addItem(existing);
        } else {
            existing.setQuantity(newQuantity);
        }
        cart.incrementVersion();
        cartRepository.saveAndFlush(cart);
        return toItemResponse(existing, product);
    }

    @Transactional
    public ItemCarritoResponse updateItem(String customerId, String itemId,
            ActualizarCantidadRequest request) {
        Cart cart = findOrCreateCart(customerId);
        String productId = normalizeUuid(itemId);
        CartItem item = cart.getItems().stream()
                .filter(current -> productId.equals(current.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El producto no existe en el carrito."));

        if (request.quantity() > CANTIDAD_MAXIMA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad máxima por producto es 99.");
        }
        if (totalUnits(cart, item, request.quantity()) > UNIDADES_MAXIMAS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El carrito admite como máximo 500 unidades.");
        }

        item.setQuantity(request.quantity());
        cart.incrementVersion();
        cartRepository.saveAndFlush(cart);
        CatalogoProductResponse product = catalogoClient.findProduct(productId);
        if (product == null || product.titulo() == null || product.titulo().isBlank()
                || product.precio() == null || product.precio() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "ms-catalogo devolvió datos de producto incompletos.");
        }
        return toItemResponse(item, product);
    }

    @Transactional
    public void removeItem(String customerId, String itemId) {
        Cart cart = findOrCreateCart(customerId);
        String productId = normalizeUuid(itemId);
        boolean removed = cart.getItems().removeIf(item -> productId.equals(item.getProductId()));
        if (!removed) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El producto no existe en el carrito.");
        }
        cart.incrementVersion();
        cartRepository.saveAndFlush(cart);
    }

    @Transactional
    public void clearCart(String customerId) {
        Cart cart = findOrCreateCart(customerId);
        if (!cart.getItems().isEmpty()) {
            cart.clear();
            cart.incrementVersion();
            cartRepository.saveAndFlush(cart);
        }
    }

    @Transactional
    public CouponCalculationResponse applyCoupon(String customerId, AplicarCuponRequest request) {
        if (couponRepository == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "La persistencia de cupones no está disponible.");
        }
        Cart cart = findOrCreateCart(customerId);
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActive(code, 1)
                .filter(Coupon::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El cupón no existe o está inactivo."));
        cart.setCoupon(coupon);
        cart.incrementVersion();
        cartRepository.saveAndFlush(cart);
        return calculateCoupon(cart, coupon);
    }

    @Transactional
    public void removeCoupon(String customerId) {
        Cart cart = findOrCreateCart(customerId);
        if (cart.getCoupon() != null) {
            cart.setCoupon(null);
            cart.incrementVersion();
            cartRepository.saveAndFlush(cart);
        }
    }

    private CouponCalculationResponse calculateCoupon(Cart cart, Coupon coupon) {
        long subtotal = 0;
        for (CartItem item : cart.getItems()) {
            CatalogoProductResponse product = catalogoClient.findProduct(item.getProductId());
            if (product == null || product.precio() == null || product.precio() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "ms-catalogo devolvió un precio inválido.");
            }
            subtotal = Math.addExact(subtotal,
                    Math.multiplyExact((long) product.precio(), item.getQuantity()));
        }

        BigDecimal discount = BigDecimal.valueOf(subtotal)
                .multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        long discountClp = discount.longValueExact();
        long total = subtotal - discountClp;
        return new CouponCalculationResponse(
                coupon.getCode(),
                coupon.getDiscountPercent(),
                subtotal,
                discountClp,
                total);
    }

    private Cart findOrCreateCart(String customerId) {
        String normalizedUserId = normalizeUuid(customerId);
        return cartRepository.findByUserIdAndStatus(normalizedUserId, Cart.STATUS_ACTIVE)
                .orElseGet(() -> cartRepository.saveAndFlush(new Cart(normalizedUserId)));
    }

    private int totalUnits(Cart cart, CartItem changedItem, int changedQuantity) {
        int total = 0;
        for (CartItem item : cart.getItems()) {
            int quantity = item == changedItem ? changedQuantity : item.getQuantity();
            total = Math.addExact(total, quantity);
        }
        return total;
    }

    private CarritoResponse toResponse(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<ItemCarritoResponse> items = new java.util.ArrayList<>();
        for (CartItem item : cart.getItems()) {
            CatalogoProductResponse product = catalogoClient.findProduct(item.getProductId());
            if (product == null || product.precio() == null || product.precio() <= 0
                    || product.titulo() == null || product.titulo().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "ms-catalogo devolvió datos de producto incompletos.");
            }
            ItemCarritoResponse response = toItemResponse(item, product);
            items.add(response);
            subtotal = subtotal.add(BigDecimal.valueOf(response.subtotal()));
        }

        long subtotalClp = subtotal.setScale(0, RoundingMode.HALF_UP).longValueExact();
        long discountClp = 0;
        if (cart.getCoupon() != null) {
            discountClp = subtotal
                    .multiply(BigDecimal.valueOf(cart.getCoupon().getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    .longValueExact();
        }
        long totalClp = subtotalClp - discountClp;
        CartCouponResponse coupon = cart.getCoupon() == null
                ? null
                : new CartCouponResponse(
                        cart.getCoupon().getCode(),
                        cart.getCoupon().getDiscountPercent());
        long includedVat = BigDecimal.valueOf(totalClp)
                .multiply(BigDecimal.valueOf(19))
                .divide(BigDecimal.valueOf(119), 0, RoundingMode.HALF_UP)
                .longValueExact();
        return new CarritoResponse(
                cart.getVersion(),
                items,
                coupon,
                subtotalClp,
                discountClp,
                includedVat,
                totalClp);
    }

    private static CarritoResponse emptyCartResponse() {
        return new CarritoResponse(0, List.of(), null, 0L, 0L, 0L, 0L);
    }

    private static ItemCarritoResponse toItemResponse(
            CartItem item,
            CatalogoProductResponse product) {
        long subtotal = BigDecimal.valueOf(product.precio())
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return new ItemCarritoResponse(
                item.getProductId(),
                product.titulo(),
                product.imagen(),
                (long) product.precio(),
                item.getQuantity(),
                subtotal);
    }

    private static String normalizeUuid(String value) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El identificador UUID es obligatorio.");
        }
        try {
            return UUID.fromString(value.trim()).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador debe tener formato UUID.",
                    exception);
        }
    }
}
