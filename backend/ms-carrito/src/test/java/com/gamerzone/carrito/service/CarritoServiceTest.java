package com.gamerzone.carrito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.gamerzone.carrito.dto.request.ActualizarCantidadRequest;
import com.gamerzone.carrito.dto.request.AgregarProductoRequest;
import com.gamerzone.carrito.entity.Cart;
import com.gamerzone.carrito.entity.CartItem;
import com.gamerzone.carrito.integration.CatalogoClient;
import com.gamerzone.carrito.integration.CatalogoProductResponse;
import com.gamerzone.carrito.repository.CartRepository;
import com.gamerzone.carrito.repository.CouponRepository;
import com.gamerzone.carrito.entity.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    private static final String USER_ID = "11111111-1111-4111-8111-111111111111";
    private static final String PRODUCT_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private CouponRepository couponRepository;

    private CarritoService service;

    @BeforeEach
    void setUp() {
        service = new CarritoService(cartRepository, catalogoClient, couponRepository);
    }

    @Test
    void findCartCreatesCartWhenUserHasNoActiveCart() {
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.saveAndFlush(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.findCart(USER_ID);

        assertEquals(0, response.items().size());
        verify(cartRepository).saveAndFlush(any(Cart.class));
    }

    @Test
    void addItemSumsQuantityForExistingProduct() {
        Cart cart = new Cart(USER_ID);
        cart.addItem(new CartItem(PRODUCT_ID, 2));
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogoClient.findProduct(PRODUCT_ID))
                .thenReturn(new CatalogoProductResponse(PRODUCT_ID, "Juego Uno", 1000));
        when(cartRepository.saveAndFlush(cart)).thenReturn(cart);

        var response = service.addItem(USER_ID, new AgregarProductoRequest(PRODUCT_ID, 3));

        assertEquals(5, response.cantidad());
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void addItemRejectsQuantityAbove99() {
        Cart cart = new Cart(USER_ID);
        cart.addItem(new CartItem(PRODUCT_ID, 97));
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogoClient.findProduct(PRODUCT_ID))
                .thenReturn(new CatalogoProductResponse(PRODUCT_ID, "Juego Uno", 1000));

        assertThrows(ResponseStatusException.class,
                () -> service.addItem(USER_ID, new AgregarProductoRequest(PRODUCT_ID, 3)));
    }

    @Test
    void updateItemChangesQuantityAndVersion() {
        Cart cart = new Cart(USER_ID);
        cart.addItem(new CartItem(PRODUCT_ID, 2));
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartRepository.saveAndFlush(cart)).thenReturn(cart);
        when(catalogoClient.findProduct(PRODUCT_ID))
                .thenReturn(new CatalogoProductResponse(PRODUCT_ID, "Juego Uno", 1000));

        var response = service.updateItem(USER_ID, PRODUCT_ID,
                new ActualizarCantidadRequest(7));

        assertEquals(7, response.cantidad());
        assertEquals(1, cart.getVersion());
    }

    @Test
    void removeItemAndClearCartUpdateContent() {
        Cart cart = new Cart(USER_ID);
        cart.addItem(new CartItem(PRODUCT_ID, 2));
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartRepository.saveAndFlush(cart)).thenReturn(cart);

        service.removeItem(USER_ID, PRODUCT_ID);

        assertEquals(0, cart.getItems().size());

        cart.addItem(new CartItem(PRODUCT_ID, 1));
        service.clearCart(USER_ID);
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void applyCouponRoundsDiscountWithHalfUp() {
        Cart cart = new Cart(USER_ID);
        cart.addItem(new CartItem(PRODUCT_ID, 1));
        Coupon coupon = org.mockito.Mockito.mock(Coupon.class);
        when(coupon.isActive()).thenReturn(true);
        when(coupon.getCode()).thenReturn("GAMER10");
        when(coupon.getDiscountPercent()).thenReturn(10);
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(couponRepository.findByCodeIgnoreCaseAndActive("GAMER10", 1))
                .thenReturn(Optional.of(coupon));
        when(catalogoClient.findProduct(PRODUCT_ID))
                .thenReturn(new CatalogoProductResponse(PRODUCT_ID, "Juego Uno", 25990));
        when(cartRepository.saveAndFlush(cart)).thenReturn(cart);

        var response = service.applyCoupon(USER_ID,
                new com.gamerzone.carrito.dto.request.AplicarCuponRequest("gamer10"));

        assertEquals(25990, response.subtotal());
        assertEquals(2599, response.discount());
        assertEquals(23391, response.total());
    }

    @Test
    void findCartReturnsCatalogTitlesPricesAndTotals() {
        Cart cart = new Cart(USER_ID);
        cart.addItem(new CartItem(PRODUCT_ID, 2));
        Coupon coupon = org.mockito.Mockito.mock(Coupon.class);
        when(coupon.getDiscountPercent()).thenReturn(10);
        cart.setCoupon(coupon);
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogoClient.findProduct(PRODUCT_ID))
                .thenReturn(new CatalogoProductResponse(PRODUCT_ID, "Juego Uno", 25990));

        var response = service.findCart(USER_ID);

        assertEquals("Juego Uno", response.items().get(0).titulo());
        assertEquals(25990L, response.items().get(0).precioUnitario());
        assertEquals(2, response.items().get(0).cantidad());
        assertEquals(51980L, response.items().get(0).subtotal());
        assertEquals(51980L, response.subtotal());
        assertEquals(5198L, response.descuento());
        assertEquals(46782L, response.total());
    }
}
