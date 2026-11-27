package cl.gamerzone.catalogo.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SystemRequirementsId implements Serializable {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "product_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID productId;

    @Column(name = "requirement_kind", nullable = false, length = 12)
    private String requirementKind;

    protected SystemRequirementsId() {
    }

    public SystemRequirementsId(UUID productId, String requirementKind) {
        this.productId = productId;
        this.requirementKind = requirementKind;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getRequirementKind() {
        return requirementKind;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SystemRequirementsId that)) {
            return false;
        }
        return Objects.equals(productId, that.productId)
                && Objects.equals(requirementKind, that.requirementKind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, requirementKind);
    }
}
