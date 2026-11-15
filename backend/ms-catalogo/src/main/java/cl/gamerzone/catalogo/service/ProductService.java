package cl.gamerzone.catalogo.service;

import java.util.List;

import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.ProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductResponse;
import cl.gamerzone.catalogo.dto.product.ProductUpdateRequest;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    /**
     * Las búsquedas paginadas y sin paginar, la consulta por ID, la creación, la actualización
     * completa y el archivado lógico dependen del modelo relacional aprobado. No se simula la
     * persistencia; implementar esos métodos con repositorio, transacciones y consultas paginadas
     * cuando el modelo esté disponible.
     */
    public List<ProductResponse> findAll() {
        throw persistenceNotConfigured();
    }

    public ProductPageResponse findAll(int page, int size) {
        throw persistenceNotConfigured();
    }

    public ProductDetailResponse findById(Long id) {
        throw persistenceNotConfigured();
    }

    public ProductDetailResponse create(ProductCreateRequest request) {
        throw persistenceNotConfigured();
    }

    public ProductDetailResponse update(Long id, ProductUpdateRequest request) {
        throw persistenceNotConfigured();
    }

    public void archive(Long id) {
        throw persistenceNotConfigured();
    }

    private IllegalStateException persistenceNotConfigured() {
        return new IllegalStateException(
                "La persistencia de productos está pendiente de la aprobación del modelo relacional.");
    }
}
