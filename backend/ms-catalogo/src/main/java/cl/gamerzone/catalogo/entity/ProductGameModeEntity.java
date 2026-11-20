package cl.gamerzone.catalogo.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_game_modes")
public class ProductGameModeEntity {

    @EmbeddedId
    private ProductGameModeId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @MapsId("modeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mode_id", nullable = false)
    private GameModeEntity mode;

    protected ProductGameModeEntity() {
    }

    public ProductGameModeEntity(ProductEntity product, GameModeEntity mode) {
        this.id = new ProductGameModeId(null, null);
        this.product = product;
        this.mode = mode;
    }

    public ProductGameModeId getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public GameModeEntity getMode() {
        return mode;
    }

    public void setMode(GameModeEntity mode) {
        this.mode = mode;
    }
}
