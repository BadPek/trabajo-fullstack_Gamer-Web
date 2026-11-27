package com.gamerzone.carrito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.gamerzone.carrito.entity.Cart;
import com.gamerzone.carrito.entity.CartItem;
import com.gamerzone.carrito.integration.CatalogoClient;
import com.gamerzone.carrito.integration.CatalogoProductResponse;
import com.gamerzone.carrito.repository.CartPriceQuoteRepository;
import com.gamerzone.carrito.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CartQuoteServiceTest {

    private static final String USER_ID = "11111111-1111-4111-8111-111111111111";
    private static final String CART_ID = "22222222-2222-4222-8222-222222222222";
    private static final String PRODUCT_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartPriceQuoteRepository quoteRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Test
    void createQuoteReturnsTokenAndTenMinuteExpiration() {
        Cart cart = new Cart(USER_ID);
        ReflectionTestUtils.setField(cart, "id", CART_ID);
        cart.addItem(new CartItem(PRODUCT_ID, 2));
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(catalogoClient.findProduct(PRODUCT_ID))
                .thenReturn(new CatalogoProductResponse(PRODUCT_ID, "Juego Uno", 1000));
        when(quoteRepository.saveAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CartQuoteService service = new CartQuoteService(
                cartRepository,
                quoteRepository,
                catalogoClient,
                "clave-de-prueba");

        var response = service.create(USER_ID);

        assertEquals(0, response.cartVersion());
        assertNotNull(response.priceQuoteToken());
        assertNotNull(response.expiresAt());
        assertEquals(PRODUCT_ID, response.items().get(0).productId());
        assertEquals(2, response.items().get(0).quantity());
        verify(quoteRepository).saveAndFlush(any());
    }
}
