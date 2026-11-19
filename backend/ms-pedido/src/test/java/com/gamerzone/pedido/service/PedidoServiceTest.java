package com.gamerzone.pedido.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.gamerzone.pedido.exception.ApiException;
import com.gamerzone.pedido.integration.CarritoClient;
import com.gamerzone.pedido.integration.CatalogoClient;
import com.gamerzone.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private PedidoWriter pedidoWriter;
    @Mock
    private CarritoClient carritoClient;
    @Mock
    private CatalogoClient catalogoClient;

    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(pedidoRepository, pedidoWriter, carritoClient, catalogoClient);
    }

    @Test
    void retryWithSameKeyReplaysStoredSnapshotWithoutCallingDependencies() {
        Pedido stored = new Pedido("cliente@example.com", "checkout-1",
                PedidoFingerprint.sha256("Calle 1"), "Calle 1", 2500);
        when(pedidoRepository.findByCustomerIdAndIdempotencyKey("cliente@example.com", "checkout-1"))
                .thenReturn(Optional.of(stored));

        var response = service.create("cliente@example.com", "jwt", "checkout-1",
                new CrearPedidoRequest("Calle 1"));

        assertEquals(2500, response.total());
        assertEquals("Calle 1", response.direccionDespacho());
        verifyNoInteractions(carritoClient, catalogoClient, pedidoWriter);
    }

    @Test
    void reusingKeyWithDifferentRequestIsRejectedBeforeCheckout() {
        Pedido stored = new Pedido("cliente@example.com", "checkout-1",
                PedidoFingerprint.sha256("Calle 1"), "Calle 1", 2500);
        when(pedidoRepository.findByCustomerIdAndIdempotencyKey("cliente@example.com", "checkout-1"))
                .thenReturn(Optional.of(stored));

        ApiException error = assertThrows(ApiException.class, () -> service.create(
                "cliente@example.com", "jwt", "checkout-1", new CrearPedidoRequest("Calle 2")));

        assertEquals("IDEMPOTENCY_KEY_REUSED", error.code());
        verifyNoInteractions(carritoClient, catalogoClient, pedidoWriter);
    }

    @Test
    void checkoutSnapshotsCatalogPricesAndCalculatesTotalFromCartQuantities() {
        when(pedidoRepository.findByCustomerIdAndIdempotencyKey("cliente@example.com", "checkout-2"))
                .thenReturn(Optional.empty());
        when(carritoClient.getCurrentCart("jwt"))
                .thenReturn(new CarritoResponse(List.of(
                        new CarritoResponse.ItemCarrito("line-1", "1", 2),
                        new CarritoResponse.ItemCarrito("line-2", "2", 1))));
        when(catalogoClient.findProduct("1")).thenReturn(new ProductoResponse(1L, "Juego Uno", 1000));
        when(catalogoClient.findProduct("2")).thenReturn(new ProductoResponse(2L, "Juego Dos", 500));
        when(pedidoWriter.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create("cliente@example.com", "jwt", "checkout-2",
                new CrearPedidoRequest("Dirección de despacho"));

        assertEquals(2500, response.total());
        assertEquals(2, response.items().size());
        assertEquals(2000, response.items().get(0).subtotal());
        assertEquals(1000, response.items().get(0).precioUnitario());
        assertEquals("Juego Uno", response.items().get(0).nombreProducto());

        ArgumentCaptor<Pedido> saved = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoWriter).save(saved.capture());
        assertEquals(2500, saved.getValue().getTotalAmount());
        assertEquals(2, saved.getValue().getItems().size());
        verify(catalogoClient).findProduct("1");
        verify(catalogoClient).findProduct("2");
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}
