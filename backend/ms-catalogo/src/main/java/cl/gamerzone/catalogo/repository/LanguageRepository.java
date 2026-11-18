package cl.gamerzone.catalogo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.LanguageEntity;

public interface LanguageRepository extends JpaRepository<LanguageEntity, UUID> {

    Optional<LanguageEntity> findByNameIgnoreCase(String name);
}
