package com.gamerzone.auth.repository;

import java.util.Optional;

import com.gamerzone.auth.entity.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunaRepository extends JpaRepository<Comuna, Integer> {

    /** Busca la comuna por el código de su región ({@code metropolitana}) y su nombre, sin distinguir mayúsculas. */
    Optional<Comuna> findByRegion_CodeAndNameIgnoreCase(String regionCode, String name);
}
