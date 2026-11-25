package com.gamerzone.contacto.repository;

import com.gamerzone.contacto.entity.ContactoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoMensajeRepository extends JpaRepository<ContactoMensaje, String> {
}
