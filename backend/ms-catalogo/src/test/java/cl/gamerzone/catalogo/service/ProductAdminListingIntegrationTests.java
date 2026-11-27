package cl.gamerzone.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductDetails;
import cl.gamerzone.catalogo.dto.product.ProductRequirements;
import cl.gamerzone.catalogo.dto.product.ProductStatus;
import cl.gamerzone.catalogo.dto.product.ProductUpdateRequest;
import cl.gamerzone.catalogo.dto.product.SystemRequirements;
import cl.gamerzone.catalogo.entity.ProductEntity;
import cl.gamerzone.catalogo.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:catalogue-admin-list-test;DB_CLOSE_DELAY=-1"
})
@Sql(statements = {
        "ALTER TABLE products ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP",
        "ALTER TABLE products ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP"
})
class ProductAdminListingIntegrationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearsProductsFromPreviousTest() {
        productRepository.deleteAll();
    }

    @Test
    void listsActiveAndArchivedProductsWithStatusFilterAndContractOrdering() {
        var archived = productService.create(request("Archived game"));
        var activeOne = productService.create(request("Active game one"));
        var activeTwo = productService.create(request("Active game two"));

        ProductEntity archivedEntity = productRepository.findById(archived.id()).orElseThrow();
        archivedEntity.setStatus(ProductStatus.ARCHIVED.name());
        productRepository.saveAndFlush(archivedEntity);

        Timestamp newerCreatedAt = Timestamp.valueOf(LocalDateTime.of(2026, 1, 2, 0, 0));
        jdbcTemplate.update(
                "UPDATE products SET created_at = ? WHERE product_id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 0, 0)),
                archived.id().toString());
        jdbcTemplate.update(
                "UPDATE products SET created_at = ? WHERE product_id = ?",
                newerCreatedAt,
                activeOne.id().toString());
        jdbcTemplate.update(
                "UPDATE products SET created_at = ? WHERE product_id = ?",
                newerCreatedAt,
                activeTwo.id().toString());

        List<UUID> activeIds = List.of(activeOne.id(), activeTwo.id()).stream()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        var firstPage = productService.findAdmin(0, 1, null);
        var secondPage = productService.findAdmin(1, 1, null);
        var thirdPage = productService.findAdmin(2, 1, null);
        var archivedOnly = productService.findAdmin(0, 10, ProductStatus.ARCHIVED);

        assertEquals(3, firstPage.totalElements());
        assertEquals(3, firstPage.totalPages());
        assertEquals(activeIds.get(0), firstPage.content().get(0).id());
        assertEquals(ProductStatus.ACTIVE, firstPage.content().get(0).status());
        assertEquals("Action RPG", firstPage.content().get(0).categoria());
        assertEquals("src/img/cover.png", firstPage.content().get(0).imagen());
        assertEquals(activeIds.get(1), secondPage.content().get(0).id());
        assertEquals(ProductStatus.ACTIVE, secondPage.content().get(0).status());
        assertEquals(archived.id(), thirdPage.content().get(0).id());
        assertEquals(ProductStatus.ARCHIVED, thirdPage.content().get(0).status());
        assertEquals(1, archivedOnly.totalElements());
        assertEquals(archived.id(), archivedOnly.content().get(0).id());
    }

    @Test
    void updatesProductRelationsAndPreservesItsStatus() {
        var created = productService.create(request("Original game"));
        ProductEntity original = productRepository.findById(created.id()).orElseThrow();
        original.setStatus(ProductStatus.ARCHIVED.name());
        productRepository.saveAndFlush(original);

        var updated = productService.update(created.id(), updateRequest());
        var persisted = productService.findAdminById(created.id());

        assertEquals(ProductStatus.ARCHIVED, updated.status());
        assertEquals("Updated game", updated.titulo());
        assertEquals(69990, updated.precio());
        assertEquals("Strategy", updated.categoria());
        assertEquals("Descripción corta actualizada", updated.descripcionCorta());
        assertEquals("Descripción completa actualizada", updated.descripcion());
        assertEquals("src/img/cover.png", updated.imagen());
        assertEquals(List.of("src/img/gallery.png"), updated.galeria());
        assertEquals("FromSoftware", updated.detalles().desarrollador());
        assertEquals(LocalDate.of(2026, 4, 4), updated.detalles().fechaLanzamiento());
        assertEquals(List.of("Un jugador"), updated.detalles().modos());
        assertEquals(List.of("Español"), updated.detalles().idiomas());
        assertEquals("16 GB", updated.requisitos().minimos().memoria());
        assertEquals(ProductStatus.ARCHIVED, persisted.status());
        assertEquals("Strategy", persisted.categoria());
    }

    @Test
    void archivesAndReactivatesProductWithoutDeletingItsDetails() {
        var created = productService.create(request("Lifecycle game"));

        productService.archive(created.id());

        assertEquals(ProductStatus.ARCHIVED, productService.findAdminById(created.id()).status());
        assertNotFound(() -> productService.findById(created.id()));

        var reactivated = productService.updateStatus(created.id(), ProductStatus.ACTIVE);

        assertEquals(ProductStatus.ACTIVE, reactivated.status());
        assertEquals(created.id(), productService.findById(created.id()).id());
    }

    @Test
    void returnsNotFoundForMissingAdministrativeProductOperations() {
        var missingId = UUID.randomUUID();

        assertNotFound(() -> productService.findAdminById(missingId));
        assertNotFound(() -> productService.update(missingId, updateRequest()));
        assertNotFound(() -> productService.archive(missingId));
        assertNotFound(() -> productService.updateStatus(missingId, ProductStatus.ARCHIVED));
    }

    private ProductCreateRequest request(String title) {
        SystemRequirements minimum = new SystemRequirements(
                "Windows 10", "CPU mínimo", "8 GB", "GPU mínima", "DirectX 12", "60 GB");
        SystemRequirements recommended = new SystemRequirements(
                "Windows 11", "CPU recomendado", "16 GB", "GPU recomendada", "DirectX 12", "60 GB");
        return new ProductCreateRequest(
                title,
                59990,
                "Action RPG",
                "src/img/cover.png",
                "Descripción breve",
                "Descripción completa",
                null,
                List.of("src/img/gallery.png"),
                new ProductDetails(
                        "FromSoftware",
                        "Bandai Namco",
                        LocalDate.of(2025, 1, 1),
                        List.of("Un jugador"),
                        List.of("Español")),
                new ProductRequirements(minimum, recommended));
    }

    private ProductUpdateRequest updateRequest() {
        SystemRequirements minimum = new SystemRequirements(
                "Windows 10", "CPU mínimo", "16 GB", "GPU mínima", "DirectX 12", "60 GB");
        SystemRequirements recommended = new SystemRequirements(
                "Windows 11", "CPU recomendado", "32 GB", "GPU recomendada", "DirectX 12", "60 GB");
        return new ProductUpdateRequest(
                "Updated game",
                69990,
                "Strategy",
                "src/img/cover.png",
                "Descripción corta actualizada",
                "Descripción completa actualizada",
                null,
                List.of("src/img/gallery.png"),
                new ProductDetails(
                        "FromSoftware",
                        "Bandai Namco",
                        LocalDate.of(2026, 4, 4),
                        List.of("Un jugador"),
                        List.of("Español")),
                new ProductRequirements(minimum, recommended));
    }

    private void assertNotFound(Runnable action) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
