package com.gamerzone.pedido.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.gamerzone.pedido.dto.integration.QuoteValidationResponse;
import com.gamerzone.pedido.dto.integration.ReservationResponse;
import com.gamerzone.pedido.dto.request.CrearPedidoRequest;
import com.gamerzone.pedido.entity.Pedido;
import com.gamerzone.pedido.entity.PedidoIdempotencia;
import com.gamerzone.pedido.entity.PedidoIdempotenciaId;
import com.gamerzone.pedido.entity.PedidoItem;
import com.gamerzone.pedido.exception.ApiException;
import com.gamerzone.pedido.integration.CarritoClient;
import com.gamerzone.pedido.integration.CatalogoClient;
import com.gamerzone.pedido.repository.PedidoIdempotenciaRepository;
import com.gamerzone.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final String CLIENTE = "11111111-1111-4111-8111-111111111111";
    private static final String CLAVE = "22222222-2222-4222-8222-222222222222";
    private static final String PEDIDO_ID = "33333333-3333-4333-8333-333333333333";
    private static final String PRODUCTO_1 = "b0000000-0000-4000-8000-000000000001";
    private static final String PRODUCTO_2 = "b0000000-0000-4000-8000-000000000002";

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoIdempotenciaRepository idempotenciaRepository;

    @Mock
    private PedidoWriter pedidoWriter;

    @Mock
    private CarritoClient carritoClient;

    @Mock
    private CatalogoClient catalogoClient;

    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(pedidoRepository, idempotenciaRepository, pedidoWriter,
                carritoClient, catalogoClient);
    }

    @Test
    void retryWithSameKeyVersionAndQuoteReplaysStoredOrderWithoutCallingDependencies() {
        when(idempotenciaRepository.findById(new PedidoIdempotenciaId(CLIENTE, CLAVE)))
                .thenReturn(Optional.of(new PedidoIdempotencia(CLIENTE, CLAVE, 7,
                        PedidoFingerprint.sha256("cotizacion-1"), PEDIDO_ID)));
        when(pedidoRepository.findByIdAndCustomerId(PEDIDO_ID, CLIENTE)).thenReturn(Optional.of(storedOrder()));

        var response = service.create(CLIENTE, "jwt", CLAVE, new CrearPedidoRequest(7L, "cotizacion-1"));

        assertEquals(PEDIDO_ID, response.id());
        assertEquals(2500, response.total());
        assertEquals("REGISTRADO_SIN_PAGO", response.estado());
        verifyNoInteractions(carritoClient, catalogoClient, pedidoWriter);
    }

    @Test
    void reusingKeyWithAnotherCartVersionIsRejectedBeforeCheckout() {
        when(idempotenciaRepository.findById(new PedidoIdempotenciaId(CLIENTE, CLAVE)))
                .thenReturn(Optional.of(new PedidoIdempotencia(CLIENTE, CLAVE, 7,
                        PedidoFingerprint.sha256("cotizacion-1"), PEDIDO_ID)));

        ApiException error = assertThrows(ApiException.class, () -> service.create(
                CLIENTE, "jwt", CLAVE, new CrearPedidoRequest(8L, "cotizacion-1")));

        assertEquals("IDEMPOTENCY_KEY_REUSED", error.code());
        verifyNoInteractions(carritoClient, catalogoClient, pedidoWriter);
    }

    @Test
    void checkoutSnapshotsCatalogPricesAndStoresIdempotencyData() {
        when(idempotenciaRepository.findById(new PedidoIdempotenciaId(CLIENTE, CLAVE))).thenReturn(Optional.empty());
        when(carritoClient.validateQuote("jwt", 7L, "cotizacion-1"))
                .thenReturn(new QuoteValidationResponse(
                        7L,
                        null,
                        List.of(
                                new QuoteValidationResponse.QuoteItemSnapshot(PRODUCTO_1, 2, "Juego Uno", 1000),
                                new QuoteValidationResponse.QuoteItemSnapshot(PRODUCTO_2, 1, "Juego Dos", 500)),
                        null));
        when(carritoClient.createReservation("jwt", 7L, CLAVE))
                .thenReturn(new ReservationResponse("reservation-1", 7L, "ACTIVE", null));
        when(pedidoWriter.save(any(Pedido.class), anyString(), anyLong(), anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // La clave llega en mayúsculas y se guarda normalizada en minúsculas.
        var response = service.create(CLIENTE, "jwt", CLAVE.toUpperCase(), new CrearPedidoRequest(7L, "cotizacion-1"));

        assertEquals(2500, response.total());
        assertEquals("CLP", response.moneda());
        assertEquals("REGISTRADO_SIN_PAGO", response.estado());
        assertEquals(2, response.items().size());
        assertEquals(2000, response.items().get(0).subtotal());
        assertEquals(1000, response.items().get(0).precioUnitario());
        assertEquals("Juego Uno", response.items().get(0).nombreProducto());
        assertEquals(PRODUCTO_1, response.items().get(0).productoId());

        ArgumentCaptor<Pedido> saved = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoWriter).save(saved.capture(), eq(CLAVE), eq(7L),
                eq(PedidoFingerprint.sha256("cotizacion-1")), eq("reservation-1"));
        verify(carritoClient).consumeQuote("jwt", 7L, "cotizacion-1");
        verify(carritoClient).completeReservation("jwt", "reservation-1");
        assertEquals(CLIENTE, saved.getValue().getCustomerId());
        assertEquals(1, saved.getValue().getPricingPolicyVersion());
        assertEquals(2, saved.getValue().getItems().size());
    }

    @Test
    void emptyCartIsRejectedWithoutSavingAnOrder() {
        when(idempotenciaRepository.findById(new PedidoIdempotenciaId(CLIENTE, CLAVE))).thenReturn(Optional.empty());
        when(carritoClient.validateQuote("jwt", 7L, "cotizacion-1"))
                .thenReturn(new QuoteValidationResponse(7L, null, List.of(), null));

        ApiException error = assertThrows(ApiException.class, () -> service.create(
                CLIENTE, "jwt", CLAVE, new CrearPedidoRequest(7L, "cotizacion-1")));

        assertEquals("EMPTY_CART", error.code());
        verify(pedidoWriter, never()).save(
                any(Pedido.class), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void couponSnapshotDiscountIsRoundedFromQuoteSubtotal() {
        when(idempotenciaRepository.findById(new PedidoIdempotenciaId(CLIENTE, CLAVE)))
                .thenReturn(Optional.empty());
        when(carritoClient.validateQuote("jwt", 7L, "cotizacion-1"))
                .thenReturn(new QuoteValidationResponse(
                        7L,
                        null,
                        List.of(new QuoteValidationResponse.QuoteItemSnapshot(
                                PRODUCTO_1, 1, "Juego Uno", 25990)),
                        new QuoteValidationResponse.QuoteCouponSnapshot("GAMER10", 10)));
        when(carritoClient.createReservation("jwt", 7L, CLAVE))
                .thenReturn(new ReservationResponse("reservation-1", 7L, "ACTIVE", null));
        when(pedidoWriter.save(any(Pedido.class), anyString(), anyLong(), anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(
                CLIENTE, "jwt", CLAVE, new CrearPedidoRequest(7L, "cotizacion-1"));

        assertEquals(23391, response.total());
    }

    @Test
    void confirmationFailurePreservesSavedOrder() {
        when(idempotenciaRepository.findById(new PedidoIdempotenciaId(CLIENTE, CLAVE)))
                .thenReturn(Optional.empty());
        when(carritoClient.validateQuote("jwt", 7L, "cotizacion-1"))
                .thenReturn(new QuoteValidationResponse(
                        7L,
                        null,
                        List.of(new QuoteValidationResponse.QuoteItemSnapshot(
                                PRODUCTO_1, 1, "Juego Uno", 1000)),
                        null));
        when(carritoClient.createReservation("jwt", 7L, CLAVE))
                .thenReturn(new ReservationResponse("reservation-1", 7L, "ACTIVE", null));
        when(pedidoWriter.save(any(Pedido.class), anyString(), anyLong(), anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(new ApiException(
                org.springframework.http.HttpStatus.GONE,
                "RESERVATION_EXPIRED",
                "La reserva expiró"))
                .when(carritoClient).completeReservation("jwt", "reservation-1");

        assertThrows(ApiException.class, () -> service.create(
                CLIENTE, "jwt", CLAVE, new CrearPedidoRequest(7L, "cotizacion-1")));

        verify(pedidoWriter).save(
                any(Pedido.class), eq(CLAVE), eq(7L),
                eq(PedidoFingerprint.sha256("cotizacion-1")), eq("reservation-1"));
        verify(carritoClient, never()).releaseReservation("jwt", "reservation-1");
    }

    private static Pedido storedOrder() {
        Pedido pedido = new Pedido(CLIENTE);
        pedido.addItem(new PedidoItem(PRODUCTO_1, "Juego Uno", 2, 1000));
        pedido.addItem(new PedidoItem(PRODUCTO_2, "Juego Dos", 1, 500));
        ReflectionTestUtils.setField(pedido, "id", PEDIDO_ID);
        return pedido;
    }
}
