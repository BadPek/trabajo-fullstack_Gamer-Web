package com.gamerzone.auth.repository;

import java.util.Optional;
import java.util.List;

import com.gamerzone.auth.entity.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, String>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRut(String rut);

    boolean existsByRutAndIdNot(String rut, String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select usuario from Usuario usuario "
            + "where usuario.rol = :rol and usuario.status = :status "
            + "and usuario.anonymizedAt is null order by usuario.id")
    List<Usuario> findActiveAdministratorsForUpdate(
            @Param("rol") String rol, @Param("status") String status);
}
