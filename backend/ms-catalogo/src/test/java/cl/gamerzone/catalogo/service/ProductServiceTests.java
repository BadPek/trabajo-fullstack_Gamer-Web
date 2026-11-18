package cl.gamerzone.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cl.gamerzone.catalogo.dto.product.ProductResponse;
import cl.gamerzone.catalogo.entity.CategoryEntity;
import cl.gamerzone.catalogo.entity.CompanyEntity;
import cl.gamerzone.catalogo.entity.GameModeEntity;
import cl.gamerzone.catalogo.entity.LanguageEntity;
import cl.gamerzone.catalogo.entity.ProductCompanyEntity;
import cl.gamerzone.catalogo.entity.ProductEntity;
import cl.gamerzone.catalogo.entity.ProductGameModeEntity;
import cl.gamerzone.catalogo.entity.ProductLanguageEntity;
import cl.gamerzone.catalogo.entity.ProductMediaEntity;
import cl.gamerzone.catalogo.entity.SystemRequirementsEntity;
import cl.gamerzone.catalogo.entity.SystemRequirementsId;
import cl.gamerzone.catalogo.repository.CategoryRepository;
import cl.gamerzone.catalogo.repository.CompanyRepository;
import cl.gamerzone.catalogo.repository.GameModeRepository;
import cl.gamerzone.catalogo.repository.LanguageRepository;
import cl.gamerzone.catalogo.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    private static final UUID PRODUCT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private GameModeRepository gameModeRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void publicListingRequestsOnlyActiveProductsInContractOrder() {
        ProductResponse product = new ProductResponse(
                PRODUCT_ID, "Elden Ring", 59990, "Action RPG", "src/img/elden_ring.jpg", "Descripción breve");
        when(productRepository.findProductResponsesByStatus(eq("ACTIVE"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(1, 2), 5));

        var result = productService.findAll(1, 2);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findProductResponsesByStatus(eq("ACTIVE"), pageable.capture());
        assertEquals(1, result.page());
        assertEquals(2, result.size());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("releaseDate").getDirection());
        assertEquals(Sort.Direction.ASC, pageable.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void publicDetailMapsNormalizedRelationsAndRequirements() {
        ProductEntity product = mock(ProductEntity.class);
        CategoryEntity category = mock(CategoryEntity.class);
        ProductMediaEntity cover = media("COVER", 0, "src/img/cover.jpg");
        ProductMediaEntity galleryLate = media("GALLERY", 2, "src/img/gallery-2.jpg");
        ProductMediaEntity galleryEarly = media("GALLERY", 1, "src/img/gallery-1.jpg");
        ProductCompanyEntity developer = company("DEVELOPER", "FromSoftware");
        ProductCompanyEntity publisher = company("PUBLISHER", "Bandai Namco");
        ProductGameModeEntity singlePlayer = mode("Single player");
        ProductLanguageEntity spanish = language("Spanish");
        SystemRequirementsEntity minimum = requirements("MINIMUM", "8 GB RAM");
        SystemRequirementsEntity recommended = requirements("RECOMMENDED", "16 GB RAM");

        when(product.getId()).thenReturn(PRODUCT_ID);
        when(product.getTitle()).thenReturn("Elden Ring");
        when(product.getPriceClp()).thenReturn(59990);
        when(product.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn("Action RPG");
        when(product.getShortDescription()).thenReturn("Descripción breve");
        when(product.getDescriptionText()).thenReturn("Descripción completa");
        when(product.getVideoUrl()).thenReturn(null);
        when(product.getReleaseDate()).thenReturn(LocalDate.of(2022, 2, 25));
        when(product.getMedia()).thenReturn(List.of(cover, galleryLate, galleryEarly));
        when(product.getCompanies()).thenReturn(List.of(developer, publisher));
        when(product.getGameModes()).thenReturn(List.of(singlePlayer));
        when(product.getLanguages()).thenReturn(List.of(spanish));
        when(product.getRequirements()).thenReturn(List.of(minimum, recommended));
        when(productRepository.findByIdAndStatus(PRODUCT_ID, "ACTIVE")).thenReturn(Optional.of(product));

        var result = productService.findById(PRODUCT_ID);

        assertEquals(PRODUCT_ID, result.id());
        assertEquals("src/img/cover.jpg", result.imagen());
        assertEquals(List.of("src/img/gallery-1.jpg", "src/img/gallery-2.jpg"), result.galeria());
        assertEquals("FromSoftware", result.detalles().desarrollador());
        assertEquals("Bandai Namco", result.detalles().editor());
        assertEquals(List.of("Single player"), result.detalles().modos());
        assertEquals(List.of("Spanish"), result.detalles().idiomas());
        assertEquals("8 GB RAM", result.requisitos().minimos().memoria());
        assertEquals("16 GB RAM", result.requisitos().recomendados().memoria());
    }

    @Test
    void publicDetailReturnsNotFoundForMissingOrArchivedProduct() {
        when(productRepository.findByIdAndStatus(PRODUCT_ID, "ACTIVE")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.findById(PRODUCT_ID));

        assertEquals(404, exception.getStatusCode().value());
    }

    private ProductMediaEntity media(String role, int position, String path) {
        ProductMediaEntity media = mock(ProductMediaEntity.class);
        when(media.getMediaRole()).thenReturn(role);
        if ("GALLERY".equals(role)) {
            when(media.getPosition()).thenReturn(position);
        }
        when(media.getMediaRef()).thenReturn(path);
        return media;
    }

    private ProductCompanyEntity company(String role, String name) {
        ProductCompanyEntity relation = mock(ProductCompanyEntity.class);
        CompanyEntity company = mock(CompanyEntity.class);
        when(relation.getCompanyRole()).thenReturn(role);
        when(relation.getCompany()).thenReturn(company);
        when(company.getName()).thenReturn(name);
        return relation;
    }

    private ProductGameModeEntity mode(String name) {
        ProductGameModeEntity relation = mock(ProductGameModeEntity.class);
        GameModeEntity mode = mock(GameModeEntity.class);
        when(relation.getMode()).thenReturn(mode);
        when(mode.getName()).thenReturn(name);
        return relation;
    }

    private ProductLanguageEntity language(String name) {
        ProductLanguageEntity relation = mock(ProductLanguageEntity.class);
        LanguageEntity language = mock(LanguageEntity.class);
        when(relation.getLanguage()).thenReturn(language);
        when(language.getName()).thenReturn(name);
        return relation;
    }

    private SystemRequirementsEntity requirements(String kind, String memory) {
        SystemRequirementsEntity requirement = mock(SystemRequirementsEntity.class);
        SystemRequirementsId id = mock(SystemRequirementsId.class);
        when(requirement.getId()).thenReturn(id);
        when(id.getRequirementKind()).thenReturn(kind);
        when(requirement.getOperatingSystem()).thenReturn("Windows");
        when(requirement.getProcessor()).thenReturn("CPU");
        when(requirement.getMemory()).thenReturn(memory);
        when(requirement.getGraphics()).thenReturn("GPU");
        when(requirement.getDirectx()).thenReturn("12");
        when(requirement.getStorage()).thenReturn("60 GB");
        return requirement;
    }
}
