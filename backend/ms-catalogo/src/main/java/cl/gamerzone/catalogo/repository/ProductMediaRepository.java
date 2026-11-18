package cl.gamerzone.catalogo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.ProductMediaEntity;

public interface ProductMediaRepository extends JpaRepository<ProductMediaEntity, UUID> {
}
