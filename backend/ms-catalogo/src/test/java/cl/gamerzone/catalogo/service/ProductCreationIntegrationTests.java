package cl.gamerzone.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductDetails;
import cl.gamerzone.catalogo.dto.product.ProductRequirements;
import cl.gamerzone.catalogo.dto.product.SystemRequirements;
import cl.gamerzone.catalogo.repository.CategoryRepository;
import cl.gamerzone.catalogo.repository.CompanyRepository;
import cl.gamerzone.catalogo.repository.GameModeRepository;
import cl.gamerzone.catalogo.repository.LanguageRepository;
import cl.gamerzone.catalogo.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:catalogue-write-test;DB_CLOSE_DELAY=-1"
})
@Sql(statements = {
        "ALTER TABLE products ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP",
        "ALTER TABLE products ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP"
})
class ProductCreationIntegrationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private GameModeRepository gameModeRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Test
    void createsProductWithRelatedCatalogDataAndReusesNamesOnLaterCreates() {
        var created = productService.create(request(
                "Juego de prueba",
                " Action RPG ",
                "FromSoftware",
                "Bandai Namco",
                List.of("Un jugador", "Cooperativo"),
                List.of("Español", "Inglés")));
        var createdWithExistingReferences = productService.create(request(
                "Otro juego",
                "action rpg",
                "fromsoftware",
                "bandai namco",
                List.of("UN JUGADOR", "cooperativo"),
                List.of("español", "inglés")));

        var detail = productService.findById(created.id());

        assertEquals(2, productRepository.count());
        assertEquals(1, categoryRepository.count());
        assertEquals(2, companyRepository.count());
        assertEquals(2, gameModeRepository.count());
        assertEquals(2, languageRepository.count());
        assertEquals("ACTIVE", created.status().name());
        assertEquals("Action RPG", detail.categoria());
        assertEquals(List.of("src/img/cover.png", "src/img/gallery.png"), detail.galeria());
        assertEquals(List.of("Cooperativo", "Un jugador"), detail.detalles().modos());
        assertEquals(List.of("Español", "Inglés"), detail.detalles().idiomas());
        assertEquals("Windows 10", detail.requisitos().minimos().so());
        assertEquals("Windows 11", detail.requisitos().recomendados().so());
        assertTrue(productService.findAll(0, 20).content().stream()
                .anyMatch(product -> product.id().equals(createdWithExistingReferences.id())));
    }

    private ProductCreateRequest request(
            String title,
            String category,
            String developer,
            String publisher,
            List<String> modes,
            List<String> languages) {
        SystemRequirements minimum = new SystemRequirements(
                "Windows 10", "CPU mínimo", "8 GB", "GPU mínima", "DirectX 12", "60 GB");
        SystemRequirements recommended = new SystemRequirements(
                "Windows 11", "CPU recomendado", "16 GB", "GPU recomendada", "DirectX 12", "60 GB");
        return new ProductCreateRequest(
                title,
                59990,
                category,
                "src/img/cover.png",
                "Descripción breve",
                "Descripción completa",
                null,
                List.of("src/img/cover.png", "src/img/gallery.png"),
                new ProductDetails(
                        developer,
                        publisher,
                        LocalDate.of(2025, 1, 1),
                        modes,
                        languages),
                new ProductRequirements(minimum, recommended));
    }
}
