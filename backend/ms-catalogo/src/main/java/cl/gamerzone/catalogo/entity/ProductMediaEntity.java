package cl.gamerzone.catalogo.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_media")
public class ProductMediaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "media_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "media_ref", nullable = false, length = 2048)
    private String mediaRef;

    @Column(name = "media_type", nullable = false, length = 10)
    private String mediaType = "IMAGE";

    @Column(name = "media_role", nullable = false, length = 10)
    private String mediaRole;

    @Column(name = "position", nullable = false, precision = 3, scale = 0)
    private Integer position;

    protected ProductMediaEntity() {
    }

    public ProductMediaEntity(ProductEntity product, String mediaRef, String mediaRole, Integer position) {
        this.product = product;
        this.mediaRef = mediaRef;
        this.mediaRole = mediaRole;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public String getMediaRef() {
        return mediaRef;
    }

    public void setMediaRef(String mediaRef) {
        this.mediaRef = mediaRef;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaRole() {
        return mediaRole;
    }

    public void setMediaRole(String mediaRole) {
        this.mediaRole = mediaRole;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
