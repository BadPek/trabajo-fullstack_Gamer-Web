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

import com.gamerzone.pedido.dto.integration.CarritoResponse;
import com.gamerzone.pedido.dto.integration.ProductoResponse;
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
        when(carritoClient.getCurrentCart("jwt"))
                .thenReturn(new CarritoResponse(List.of(
                        new CarritoResponse.ItemCarrito("line-1", PRODUCTO_1, 2),
                        new CarritoResponse.ItemCarrito("line-2", PRODUCTO_2, 1))));
        when(catalogoClient.findProduct(PRODUCTO_1)).thenReturn(new ProductoResponse(PRODUCTO_1, "Juego Uno", 1000));
        when(catalogoClient.findProduct(PRODUCTO_2)).thenReturn(new ProductoResponse(PRODUCTO_2, "Juego Dos", 500));
        when(pedidoWriter.save(any(Pedido.class), anyString(), anyLong(), anyString()))
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
        verify(pedidoWriter).save(saved.capture(), eq(CLAVE), eq(7L), eq(PedidoFingerprint.sha256("cotizacion-1")));
        assertEquals(CLIENTE, saved.getValue().getCustomerId());
        assertEquals(1, saved.getValue().getPricingPolicyVersion());
        assertEquals(2, saved.getValue().getItems().size());
    }

    @Test
    void emptyCartIsRejectedWithoutSavingAnOrder() {
        when(idempotenciaRepository.findById(new PedidoIdempotenciaId(CLIENTE, CLAVE))).thenReturn(Optional.empty());
        when(carritoClient.getCurrentCart("jwt")).thenReturn(new CarritoResponse(List.of()));

        ApiException error = assertThrows(ApiException.class, () -> service.create(
                CLIENTE, "jwt", CLAVE, new CrearPedidoRequest(7L, "cotizacion-1")));

        assertEquals("EMPTY_CART", error.code());
        verify(pedidoWriter, never()).save(any(Pedido.class), anyString(), anyLong(), anyString());
    }

    private static Pedido storedOrder() {
        Pedido pedido = new Pedido(CLIENTE);
        pedido.addItem(new PedidoItem(PRODUCTO_1, "Juego Uno", 2, 1000));
        pedido.addItem(new PedidoItem(PRODUCTO_2, "Juego Dos", 1, 500));
        ReflectionTestUtils.setField(pedido, "id", PEDIDO_ID);
        return pedido;
    }
}
