package cl.gamerzone.catalogo.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductLanguageId implements Serializable {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "product_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID productId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "language_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID languageId;

    protected ProductLanguageId() {
    }

    public ProductLanguageId(UUID productId, UUID languageId) {
        this.productId = productId;
        this.languageId = languageId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getLanguageId() {
        return languageId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductLanguageId that)) {
            return false;
        }
        return Objects.equals(productId, that.productId)
                && Objects.equals(languageId, that.languageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, languageId);
    }
}
