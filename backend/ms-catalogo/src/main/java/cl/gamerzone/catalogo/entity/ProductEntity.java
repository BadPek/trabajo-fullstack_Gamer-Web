package cl.gamerzone.catalogo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "product_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "price_clp", nullable = false, precision = 10, scale = 0)
    private Integer priceClp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;

    @Lob
    @Column(name = "description_text", nullable = false)
    private String descriptionText;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "video_url", length = 2048)
    private String videoUrl;

    @Column(name = "status", nullable = false, length = 10)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductMediaEntity> media = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SystemRequirementsEntity> requirements = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCompanyEntity> companies = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductGameModeEntity> gameModes = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductLanguageEntity> languages = new ArrayList<>();

    protected ProductEntity() {
    }

    public ProductEntity(
            String title,
            Integer priceClp,
            CategoryEntity category,
            String shortDescription,
            String descriptionText,
            LocalDate releaseDate,
            String videoUrl) {
        this.title = title;
        this.priceClp = priceClp;
        this.category = category;
        this.shortDescription = shortDescription;
        this.descriptionText = descriptionText;
        this.releaseDate = releaseDate;
        this.videoUrl = videoUrl;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPriceClp() {
        return priceClp;
    }

    public void setPriceClp(Integer priceClp) {
        this.priceClp = priceClp;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ProductMediaEntity> getMedia() {
        return media;
    }

    public List<SystemRequirementsEntity> getRequirements() {
        return requirements;
    }

    public List<ProductCompanyEntity> getCompanies() {
        return companies;
    }

    public List<ProductGameModeEntity> getGameModes() {
        return gameModes;
    }

    public List<ProductLanguageEntity> getLanguages() {
        return languages;
    }
}
