package cl.gamerzone.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.ProductCompanyEntity;
import cl.gamerzone.catalogo.entity.ProductCompanyId;

public interface ProductCompanyRepository extends JpaRepository<ProductCompanyEntity, ProductCompanyId> {
}
