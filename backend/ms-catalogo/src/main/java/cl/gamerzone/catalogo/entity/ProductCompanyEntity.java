package cl.gamerzone.catalogo.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_companies")
public class ProductCompanyEntity {

    @EmbeddedId
    private ProductCompanyId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @MapsId("companyId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyEntity company;

    protected ProductCompanyEntity() {
    }

    public ProductCompanyEntity(ProductEntity product, CompanyEntity company, String companyRole) {
        this.id = new ProductCompanyId(null, null, companyRole);
        this.product = product;
        this.company = company;
    }

    public ProductCompanyId getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }

    public String getCompanyRole() {
        return id.getCompanyRole();
    }
}
