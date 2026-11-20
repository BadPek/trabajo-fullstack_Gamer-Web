package cl.gamerzone.catalogo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import cl.gamerzone.catalogo.entity.ProductEntity;
import cl.gamerzone.catalogo.dto.product.ProductResponse;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Page<ProductEntity> findAllByStatus(String status, Pageable pageable);

    @Query(value = """
            select new cl.gamerzone.catalogo.dto.product.ProductResponse(
                p.id, p.title, p.priceClp, c.name, m.mediaRef, p.shortDescription)
            from ProductEntity p
            join p.category c
            left join p.media m on m.mediaRole = 'COVER'
            where p.status = :status
            """,
            countQuery = """
                    select count(p)
                    from ProductEntity p
                    where p.status = :status
                    """)
    Page<ProductResponse> findProductResponsesByStatus(@Param("status") String status, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "media"})
    Optional<ProductEntity> findByIdAndStatus(UUID id, String status);
}
