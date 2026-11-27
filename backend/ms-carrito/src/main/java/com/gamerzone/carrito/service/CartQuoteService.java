package com.gamerzone.carrito.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.gamerzone.carrito.dto.response.CartQuoteCouponSnapshot;
import com.gamerzone.carrito.dto.response.CartQuoteItemSnapshot;
import com.gamerzone.carrito.dto.response.CartQuoteResponse;
import com.gamerzone.carrito.dto.request.ValidateQuoteRequest;
import com.gamerzone.carrito.dto.response.ValidatedQuoteResponse;
import com.gamerzone.carrito.entity.Cart;
import com.gamerzone.carrito.entity.CartItem;
import com.gamerzone.carrito.entity.CartPriceQuote;
import com.gamerzone.carrito.entity.CartQuoteItemSnapshotEntity;
import com.gamerzone.carrito.entity.CartQuoteCouponSnapshotEntity;
import com.gamerzone.carrito.entity.Coupon;
import com.gamerzone.carrito.integration.CatalogoClient;
import com.gamerzone.carrito.integration.CatalogoProductResponse;
import com.gamerzone.carrito.repository.CartPriceQuoteRepository;
import com.gamerzone.carrito.repository.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartQuoteService {

    private static final long TTL_SECONDS = 10 * 60;

    private final CartRepository cartRepository;
    private final CartPriceQuoteRepository quoteRepository;
    private final CatalogoClient catalogoClient;
    private final byte[] hmacSecret;

    public CartQuoteService(
            CartRepository cartRepository,
            CartPriceQuoteRepository quoteRepository,
            CatalogoClient catalogoClient,
            @Value("${carrito.quote.hmac-secret}") String hmacSecret) {
        this.cartRepository = cartRepository;
        this.quoteRepository = quoteRepository;
        this.catalogoClient = catalogoClient;
        this.hmacSecret = hmacSecret.getBytes(StandardCharsets.UTF_8);
        if (hmacSecret.isBlank()) {
            throw new IllegalArgumentException("La clave HMAC de cotizaciones no puede estar vacía.");
        }
    }

    @Transactional
    public CartQuoteResponse create(String customerId) {
        String normalizedUserId = UUID.fromString(customerId).toString().toLowerCase(Locale.ROOT);
        Cart cart = cartRepository.findByUserIdAndStatus(normalizedUserId, Cart.STATUS_ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El carrito activo no existe."));
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede cotizar un carrito vacío.");
        }

        java.util.List<CartQuoteItemSnapshot> snapshots = cart.getItems().stream()
                .sorted(java.util.Comparator.comparing(CartItem::getProductId))
                .map(item -> toSnapshot(item))
                .toList();

        CartQuoteCouponSnapshot couponSnapshot = cart.getCoupon() == null
                ? null
                : new CartQuoteCouponSnapshot(
                        cart.getCoupon().getCode(),
                        cart.getCoupon().getDiscountPercent());

        String fingerprint = fingerprint(snapshots, couponSnapshot);
        Instant expiresAt = Instant.now().plusSeconds(TTL_SECONDS);
        LocalDateTime expiresUtc = LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC);
        CartPriceQuote quote = new CartPriceQuote(
                cart,
                cart.getVersion(),
                fingerprint,
                "",
                expiresUtc);
        String payload = quote.getId() + "." + cart.getId() + "." + cart.getVersion()
                + "." + fingerprint;
        String signature = hmac(payload);
        String token = payload + "." + signature;
        quote.setTokenHashForPersistence(sha256(token));

        for (CartQuoteItemSnapshot snapshot : snapshots) {
            quote.addItemSnapshot(new CartQuoteItemSnapshotEntity(
                    quote,
                    snapshot.productId(),
                    snapshot.quantity(),
                    snapshot.titleSnapshot(),
                    snapshot.unitPriceClp()));
        }
        if (couponSnapshot != null) {
            quote.setCouponSnapshot(new CartQuoteCouponSnapshotEntity(
                    quote,
                    couponSnapshot.code(),
                    couponSnapshot.discountPercent()));
        }
        quoteRepository.saveAndFlush(quote);

        return new CartQuoteResponse(
                cart.getVersion(),
                token,
                expiresAt,
                snapshots,
                couponSnapshot);
    }

    @Transactional(readOnly = true)
    public ValidatedQuoteResponse validate(String customerId, ValidateQuoteRequest request) {
        String normalizedUserId = UUID.fromString(customerId).toString().toLowerCase(Locale.ROOT);
        String token = request.priceQuoteToken().trim();
        int separator = token.lastIndexOf('.');
        if (separator < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "La firma de la cotización no es válida.");
        }
        String payload = token.substring(0, separator);
        String signature = token.substring(separator + 1);
        String expectedSignature = hmac(payload);
        if (!constantTimeEquals(signature, expectedSignature)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "La firma de la cotización no es válida.");
        }

        String[] parts = payload.split("\\.", 4);
        if (parts.length != 4) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "La cotización no tiene un formato válido.");
        }
        long tokenVersion;
        try {
            tokenVersion = Long.parseLong(parts[2]);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "La cotización no tiene una versión válida.");
        }
        if (request.cartVersion() != tokenVersion) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La versión del carrito no coincide.");
        }

        String tokenHash = sha256(token);
        CartPriceQuote quote = quoteRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La cotización no existe."));
        if (!normalizedUserId.equals(quote.getCart().getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "La cotización no pertenece al usuario.");
        }
        if (quote.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La cotización ya fue utilizada.");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (quote.getExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.GONE,
                    "La cotización expiró.");
        }
        if (quote.getCartVersion() != request.cartVersion()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La versión del carrito no coincide.");
        }

        java.util.List<CartQuoteItemSnapshot> items = quote.getItemSnapshots().stream()
                .sorted(java.util.Comparator.comparing(
                        CartQuoteItemSnapshotEntity::getProductId))
                .map(snapshot -> new CartQuoteItemSnapshot(
                        snapshot.getProductId(),
                        snapshot.getQuantity(),
                        snapshot.getTitleSnapshot(),
                        snapshot.getUnitPriceClp()))
                .toList();
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "La cotización no contiene snapshots.");
        }
        CartQuoteCouponSnapshot coupon = quote.getCouponSnapshot() == null
                ? null
                : new CartQuoteCouponSnapshot(
                        quote.getCouponSnapshot().getCode(),
                        quote.getCouponSnapshot().getDiscountPercent());

        String expectedFingerprint = fingerprint(items, coupon);
        if (!constantTimeEquals(expectedFingerprint, quote.getPriceFingerprint())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Los snapshots de la cotización no coinciden.");
        }
        return new ValidatedQuoteResponse(
                quote.getCartVersion(),
                quote.getExpiresAt().toInstant(ZoneOffset.UTC),
                items,
                coupon);
    }

    @Transactional
    public void consume(String customerId, ValidateQuoteRequest request) {
        validate(customerId, request);
        String token = request.priceQuoteToken().trim();
        CartPriceQuote quote = quoteRepository.findByTokenHash(sha256(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La cotización no existe."));
        quote.consume(LocalDateTime.now(ZoneOffset.UTC));
        quoteRepository.saveAndFlush(quote);
    }

    private CartQuoteItemSnapshot toSnapshot(CartItem item) {
        CatalogoProductResponse product = catalogoClient.findProduct(item.getProductId());
        if (product == null || product.titulo() == null || product.titulo().isBlank()
                || product.precio() == null || product.precio() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "ms-catalogo devolvió datos de producto incompletos.");
        }
        return new CartQuoteItemSnapshot(
                item.getProductId(),
                item.getQuantity(),
                product.titulo().trim(),
                product.precio());
    }

    private static String fingerprint(
            java.util.List<CartQuoteItemSnapshot> items,
            CartQuoteCouponSnapshot coupon) {
        StringBuilder value = new StringBuilder();
        for (CartQuoteItemSnapshot item : items) {
            value.append(item.productId()).append('|')
                    .append(item.quantity()).append('|')
                    .append(item.titleSnapshot()).append('|')
                    .append(item.unitPriceClp()).append(';');
        }
        if (coupon != null) {
            value.append("coupon=").append(coupon.code())
                    .append('|').append(coupon.discountPercent());
        }
        return sha256(value.toString());
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("No fue posible firmar la cotización.", exception);
        }
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int index = 0; index < left.length(); index++) {
            result |= left.charAt(index) ^ right.charAt(index);
        }
        return result == 0;
    }
}
