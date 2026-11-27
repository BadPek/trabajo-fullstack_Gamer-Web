package cl.gamerzone.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.gamerzone.catalogo.entity.SystemRequirementsEntity;
import cl.gamerzone.catalogo.entity.SystemRequirementsId;

public interface SystemRequirementsRepository extends JpaRepository<SystemRequirementsEntity, SystemRequirementsId> {
}
