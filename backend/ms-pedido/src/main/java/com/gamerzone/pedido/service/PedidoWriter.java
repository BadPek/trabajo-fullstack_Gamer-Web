package com.gamerzone.pedido.service;

import com.gamerzone.pedido.entity.Pedido;
import com.gamerzone.pedido.entity.PedidoIdempotencia;
import com.gamerzone.pedido.repository.PedidoIdempotenciaRepository;
import com.gamerzone.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Guarda el pedido, sus líneas y su clave de idempotencia en una sola transacción. */
@Service
public class PedidoWriter {

    private final PedidoRepository pedidoRepository;
    private final PedidoIdempotenciaRepository idempotenciaRepository;

    public PedidoWriter(PedidoRepository pedidoRepository, PedidoIdempotenciaRepository idempotenciaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.idempotenciaRepository = idempotenciaRepository;
    }

    @Transactional
    public Pedido save(Pedido pedido, String idempotencyKey, long cartVersion, String cartFingerprint) {
        return save(pedido, idempotencyKey, cartVersion, cartFingerprint, null);
    }

    @Transactional
    public Pedido save(Pedido pedido, String idempotencyKey, long cartVersion,
            String cartFingerprint, String reservationId) {
        Pedido saved = pedidoRepository.saveAndFlush(pedido);
        idempotenciaRepository.saveAndFlush(new PedidoIdempotencia(saved.getCustomerId(), idempotencyKey,
                cartVersion, cartFingerprint, saved.getId(), reservationId));
        return saved;
    }
}
