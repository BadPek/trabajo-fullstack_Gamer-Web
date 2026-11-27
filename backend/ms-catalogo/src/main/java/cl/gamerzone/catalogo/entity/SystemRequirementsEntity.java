package cl.gamerzone.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_requirements")
public class SystemRequirementsEntity {

    @EmbeddedId
    private SystemRequirementsId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "os", nullable = false, length = 300)
    private String operatingSystem;

    @Column(name = "processor", nullable = false, length = 300)
    private String processor;

    @Column(name = "memory", nullable = false, length = 300)
    private String memory;

    @Column(name = "graphics", nullable = false, length = 300)
    private String graphics;

    @Column(name = "directx", nullable = false, length = 300)
    private String directx;

    @Column(name = "storage", nullable = false, length = 300)
    private String storage;

    protected SystemRequirementsEntity() {
    }

    public SystemRequirementsEntity(ProductEntity product, String requirementKind) {
        this.id = new SystemRequirementsId(null, requirementKind);
        this.product = product;
    }

    public SystemRequirementsId getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getGraphics() {
        return graphics;
    }

    public void setGraphics(String graphics) {
        this.graphics = graphics;
    }

    public String getDirectx() {
        return directx;
    }

    public void setDirectx(String directx) {
        this.directx = directx;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
