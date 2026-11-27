package com.gamerzone.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Región del catálogo de referencia (tabla {@code regions}, sembrada por la migración V2).
 * {@code code} es el valor que envía el formulario, por ejemplo {@code metropolitana}.
 */
@Entity
@Table(name = "regions")
public class Region {

    @Id
    @Column(name = "region_id", nullable = false, precision = 5, scale = 0)
    private Integer id;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    protected Region() {
    }

    public Region(Integer id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
