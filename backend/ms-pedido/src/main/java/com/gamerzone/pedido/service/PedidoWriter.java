package com.gamerzone.pedido.service;

import com.gamerzone.pedido.entity.Pedido;
import com.gamerzone.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoWriter {

    private final PedidoRepository pedidoRepository;

    public PedidoWriter(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pedido save(Pedido pedido) {
        return pedidoRepository.saveAndFlush(pedido);
    }
}
