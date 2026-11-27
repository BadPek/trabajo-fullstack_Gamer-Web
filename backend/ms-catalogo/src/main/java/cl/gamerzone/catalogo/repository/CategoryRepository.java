package cl.gamerzone.catalogo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByNameNormalized(String nameNormalized);
}
