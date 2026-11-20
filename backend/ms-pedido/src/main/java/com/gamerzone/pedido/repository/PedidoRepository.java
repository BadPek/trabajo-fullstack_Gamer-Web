package com.gamerzone.pedido.repository;

import java.util.List;
import java.util.Optional;

import com.gamerzone.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, String> {

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    @EntityGraph(attributePaths = "items")
    Optional<Pedido> findByIdAndCustomerId(String id, String customerId);
}
