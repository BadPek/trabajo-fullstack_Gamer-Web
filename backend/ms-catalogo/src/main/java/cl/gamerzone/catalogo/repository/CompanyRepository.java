package cl.gamerzone.catalogo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.CompanyEntity;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {

    Optional<CompanyEntity> findByNameNormalized(String nameNormalized);
}
