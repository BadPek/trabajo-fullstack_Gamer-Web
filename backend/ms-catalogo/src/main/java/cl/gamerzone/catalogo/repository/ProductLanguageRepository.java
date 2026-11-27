package cl.gamerzone.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.ProductLanguageEntity;
import cl.gamerzone.catalogo.entity.ProductLanguageId;

public interface ProductLanguageRepository extends JpaRepository<ProductLanguageEntity, ProductLanguageId> {
}
