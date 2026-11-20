package com.gamerzone.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Comuna del catálogo de referencia (tabla {@code communes}, sembrada por la migración V2).
 * La región de una cuenta se obtiene desde su comuna.
 */
@Entity
@Table(name = "communes")
public class Comuna {

    @Id
    @Column(name = "commune_id", nullable = false, precision = 5, scale = 0)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    protected Comuna() {
    }

    public Comuna(Integer id, Region region, String name) {
        this.id = id;
        this.region = region;
        this.name = name;
    }

    public Integer getId() { return id; }
    public Region getRegion() { return region; }
    public String getName() { return name; }
}
