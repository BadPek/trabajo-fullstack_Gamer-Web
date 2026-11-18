package cl.gamerzone.catalogo.service;

import java.util.List;
import java.util.UUID;

import cl.gamerzone.catalogo.dto.product.AdminProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.AdminProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.ProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductResponse;
import cl.gamerzone.catalogo.dto.product.ProductStatus;
import cl.gamerzone.catalogo.dto.product.ProductUpdateRequest;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    /**
     * La persistencia requiere migraciones Flyway y repositorios; no se simulan resultados.
     */
    public List<ProductResponse> findAll() {
        throw persistenceNotConfigured();
    }

    public ProductPageResponse findAll(int page, int size) {
        throw persistenceNotConfigured();
    }

    public AdminProductPageResponse findAdmin(int page, int size, ProductStatus status) {
        throw persistenceNotConfigured();
    }

    public ProductDetailResponse findById(UUID id) {
        throw persistenceNotConfigured();
    }

    public AdminProductDetailResponse findAdminById(UUID id) {
        throw persistenceNotConfigured();
    }

    public AdminProductDetailResponse create(ProductCreateRequest request) {
        throw persistenceNotConfigured();
    }

    public AdminProductDetailResponse update(UUID id, ProductUpdateRequest request) {
        throw persistenceNotConfigured();
    }

    public void archive(UUID id) {
        throw persistenceNotConfigured();
    }

    public AdminProductDetailResponse updateStatus(UUID id, ProductStatus status) {
        throw persistenceNotConfigured();
    }

    private IllegalStateException persistenceNotConfigured() {
        return new IllegalStateException(
                "La persistencia de productos aún no está implementada.");
    }
}
