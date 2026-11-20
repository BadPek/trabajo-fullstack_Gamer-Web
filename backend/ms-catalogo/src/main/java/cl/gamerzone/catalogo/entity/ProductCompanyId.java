package cl.gamerzone.catalogo.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductCompanyId implements Serializable {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "product_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID productId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "company_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID companyId;

    @Column(name = "company_role", nullable = false, length = 10)
    private String companyRole;

    protected ProductCompanyId() {
    }

    public ProductCompanyId(UUID productId, UUID companyId, String companyRole) {
        this.productId = productId;
        this.companyId = companyId;
        this.companyRole = companyRole;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public String getCompanyRole() {
        return companyRole;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductCompanyId that)) {
            return false;
        }
        return Objects.equals(productId, that.productId)
                && Objects.equals(companyId, that.companyId)
                && Objects.equals(companyRole, that.companyRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, companyId, companyRole);
    }
}
