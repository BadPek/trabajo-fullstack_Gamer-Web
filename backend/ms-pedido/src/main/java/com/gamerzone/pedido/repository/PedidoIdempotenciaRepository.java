package com.gamerzone.pedido.repository;

import com.gamerzone.pedido.entity.PedidoIdempotencia;
import com.gamerzone.pedido.entity.PedidoIdempotenciaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoIdempotenciaRepository extends JpaRepository<PedidoIdempotencia, PedidoIdempotenciaId> {
}
