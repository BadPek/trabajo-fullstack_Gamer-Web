package com.gamerzone.contacto.repository;

import com.gamerzone.contacto.entity.ContactoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContactoMensajeRepository extends JpaRepository<ContactoMensaje, String>,
        JpaSpecificationExecutor<ContactoMensaje> {

    long deleteByUserId(String userId);
}
