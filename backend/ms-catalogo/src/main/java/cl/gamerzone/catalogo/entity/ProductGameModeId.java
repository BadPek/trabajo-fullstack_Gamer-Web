package cl.gamerzone.catalogo.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductGameModeId implements Serializable {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "product_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID productId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "mode_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID modeId;

    protected ProductGameModeId() {
    }

    public ProductGameModeId(UUID productId, UUID modeId) {
        this.productId = productId;
        this.modeId = modeId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getModeId() {
        return modeId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductGameModeId that)) {
            return false;
        }
        return Objects.equals(productId, that.productId)
                && Objects.equals(modeId, that.modeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, modeId);
    }
}
