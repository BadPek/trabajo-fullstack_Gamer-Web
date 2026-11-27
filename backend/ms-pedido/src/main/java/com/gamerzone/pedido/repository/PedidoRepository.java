package com.gamerzone.pedido.repository;

import java.util.Optional;

import com.gamerzone.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoRepository extends JpaRepository<Pedido, String>, JpaSpecificationExecutor<Pedido> {

    Page<Pedido> findByCustomerId(String customerId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<Pedido> findByIdAndCustomerId(String id, String customerId);
}
