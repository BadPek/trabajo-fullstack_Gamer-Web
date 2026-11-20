package cl.gamerzone.catalogo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.GameModeEntity;

public interface GameModeRepository extends JpaRepository<GameModeEntity, UUID> {

    Optional<GameModeEntity> findByNameIgnoreCase(String name);
}
