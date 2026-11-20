package cl.gamerzone.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.ProductGameModeEntity;
import cl.gamerzone.catalogo.entity.ProductGameModeId;

public interface ProductGameModeRepository extends JpaRepository<ProductGameModeEntity, ProductGameModeId> {
}
