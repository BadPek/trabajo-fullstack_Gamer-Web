package cl.gamerzone.catalogo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import cl.gamerzone.catalogo.dto.product.AdminProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.AdminProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.ProductDetails;
import cl.gamerzone.catalogo.dto.product.ProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductRequirements;
import cl.gamerzone.catalogo.dto.product.ProductResponse;
import cl.gamerzone.catalogo.dto.product.ProductStatus;
import cl.gamerzone.catalogo.dto.product.ProductUpdateRequest;
import cl.gamerzone.catalogo.dto.product.SystemRequirements;
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
import cl.gamerzone.catalogo.repository.CategoryRepository;
import cl.gamerzone.catalogo.repository.CompanyRepository;
import cl.gamerzone.catalogo.repository.GameModeRepository;
import cl.gamerzone.catalogo.repository.LanguageRepository;
import cl.gamerzone.catalogo.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProductService {

    private static final String ACTIVE_STATUS = ProductStatus.ACTIVE.name();
    private static final String COVER_ROLE = "COVER";
    private static final String GALLERY_ROLE = "GALLERY";
    private static final String DEVELOPER_ROLE = "DEVELOPER";
    private static final String PUBLISHER_ROLE = "PUBLISHER";
    private static final String MINIMUM_REQUIREMENTS = "MINIMUM";
    private static final String RECOMMENDED_REQUIREMENTS = "RECOMMENDED";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final GameModeRepository gameModeRepository;
    private final LanguageRepository languageRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CompanyRepository companyRepository,
            GameModeRepository gameModeRepository,
            LanguageRepository languageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.companyRepository = companyRepository;
        this.gameModeRepository = gameModeRepository;
        this.languageRepository = languageRepository;
    }

    public List<ProductResponse> findAll() {
        throw persistenceNotConfigured();
    }

    @Transactional(readOnly = true)
    public ProductPageResponse findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("releaseDate"), Sort.Order.asc("id")));
        Page<ProductResponse> products = productRepository.findProductResponsesByStatus(ACTIVE_STATUS, pageable);
        products.forEach(this::requireCover);
        return new ProductPageResponse(
                products.getContent(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages());
    }

    public AdminProductPageResponse findAdmin(int page, int size, ProductStatus status) {
        throw persistenceNotConfigured();
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse findById(UUID id) {
        ProductEntity product = productRepository.findByIdAndStatus(id, ACTIVE_STATUS)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        return toDetailResponse(product);
    }

    public AdminProductDetailResponse findAdminById(UUID id) {
        throw persistenceNotConfigured();
    }

    @Transactional
    public AdminProductDetailResponse create(ProductCreateRequest request) {
        CategoryEntity category = findOrCreateCategory(request.categoria());
        ProductEntity product = new ProductEntity(
                trim(request.titulo()),
                request.precio(),
                category,
                trim(request.descripcionCorta()),
                trim(request.descripcion()),
                request.detalles().fechaLanzamiento(),
                trimToNull(request.video()));

        product.getCompanies().add(new ProductCompanyEntity(
                product,
                findOrCreateCompany(request.detalles().desarrollador()),
                DEVELOPER_ROLE));
        product.getCompanies().add(new ProductCompanyEntity(
                product,
                findOrCreateCompany(request.detalles().editor()),
                PUBLISHER_ROLE));

        product.getMedia().add(new ProductMediaEntity(
                product,
                trim(request.imagen()),
                COVER_ROLE,
                0));
        for (int index = 0; index < request.galeria().size(); index++) {
            product.getMedia().add(new ProductMediaEntity(
                    product,
                    trim(request.galeria().get(index)),
                    GALLERY_ROLE,
                    index + 1));
        }

        request.detalles().modos().stream()
                .map(this::trim)
                .map(this::findOrCreateGameMode)
                .map(mode -> new ProductGameModeEntity(product, mode))
                .forEach(product.getGameModes()::add);
        request.detalles().idiomas().stream()
                .map(this::trim)
                .map(this::findOrCreateLanguage)
                .map(language -> new ProductLanguageEntity(product, language))
                .forEach(product.getLanguages()::add);

        product.getRequirements().add(toEntity(product, MINIMUM_REQUIREMENTS, request.requisitos().minimos()));
        product.getRequirements().add(toEntity(product, RECOMMENDED_REQUIREMENTS, request.requisitos().recomendados()));

        ProductEntity saved = productRepository.save(product);
        return toAdminDetailResponse(saved);
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

    private void requireCover(ProductResponse product) {
        if (product.imagen() == null || product.imagen().isBlank()) {
            throw new IllegalStateException(
                    "El producto " + product.id() + " no tiene una imagen de portada.");
        }
    }

    private ProductDetailResponse toDetailResponse(ProductEntity product) {
        String cover = product.getMedia().stream()
                .filter(media -> COVER_ROLE.equals(media.getMediaRole()))
                .map(ProductMediaEntity::getMediaRef)
                .findFirst()
                .orElseThrow(() -> invalidProduct(product, "no tiene una imagen de portada"));

        List<String> gallery = product.getMedia().stream()
                .filter(media -> GALLERY_ROLE.equals(media.getMediaRole()))
                .sorted(Comparator.comparing(ProductMediaEntity::getPosition))
                .map(ProductMediaEntity::getMediaRef)
                .toList();

        String developer = companyName(product, DEVELOPER_ROLE);
        String publisher = companyName(product, PUBLISHER_ROLE);
        List<String> modes = product.getGameModes().stream()
                .map(productMode -> productMode.getMode().getName())
                .sorted()
                .toList();
        List<String> languages = product.getLanguages().stream()
                .map(productLanguage -> productLanguage.getLanguage().getName())
                .sorted()
                .toList();

        if (modes.isEmpty()) {
            throw invalidProduct(product, "no tiene modos de juego");
        }
        if (languages.isEmpty()) {
            throw invalidProduct(product, "no tiene idiomas");
        }

        Map<String, SystemRequirementsEntity> requirements = product.getRequirements().stream()
                .collect(Collectors.toMap(
                        requirement -> requirement.getId().getRequirementKind(),
                        Function.identity()));

        SystemRequirementsEntity minimum = requirements.get(MINIMUM_REQUIREMENTS);
        SystemRequirementsEntity recommended = requirements.get(RECOMMENDED_REQUIREMENTS);
        if (minimum == null || recommended == null) {
            throw invalidProduct(product, "no tiene ambos tipos de requisitos del sistema");
        }

        return new ProductDetailResponse(
                product.getId(),
                product.getTitle(),
                product.getPriceClp(),
                product.getCategory().getName(),
                cover,
                product.getShortDescription(),
                product.getDescriptionText(),
                product.getVideoUrl(),
                gallery,
                new ProductDetails(
                        developer,
                        publisher,
                        product.getReleaseDate(),
                        modes,
                        languages),
                new ProductRequirements(
                        toRequirements(minimum),
                        toRequirements(recommended)));
    }

    private AdminProductDetailResponse toAdminDetailResponse(ProductEntity product) {
        ProductDetailResponse detail = toDetailResponse(product);
        return new AdminProductDetailResponse(
                detail.id(),
                ProductStatus.valueOf(product.getStatus()),
                detail.titulo(),
                detail.precio(),
                detail.categoria(),
                detail.imagen(),
                detail.descripcionCorta(),
                detail.descripcion(),
                detail.video(),
                detail.galeria(),
                detail.detalles(),
                detail.requisitos());
    }

    private CategoryEntity findOrCreateCategory(String name) {
        String displayName = trim(name);
        String normalizedName = normalize(displayName);
        return categoryRepository.findByNameNormalized(normalizedName)
                .orElseGet(() -> categoryRepository.save(new CategoryEntity(displayName, normalizedName)));
    }

    private CompanyEntity findOrCreateCompany(String name) {
        String displayName = trim(name);
        String normalizedName = normalize(displayName);
        return companyRepository.findByNameNormalized(normalizedName)
                .orElseGet(() -> companyRepository.save(new CompanyEntity(displayName, normalizedName)));
    }

    private GameModeEntity findOrCreateGameMode(String name) {
        return gameModeRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> gameModeRepository.save(new GameModeEntity(name)));
    }

    private LanguageEntity findOrCreateLanguage(String name) {
        return languageRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> languageRepository.save(new LanguageEntity(name)));
    }

    private SystemRequirementsEntity toEntity(
            ProductEntity product,
            String requirementKind,
            SystemRequirements requirements) {
        SystemRequirementsEntity entity = new SystemRequirementsEntity(product, requirementKind);
        entity.setOperatingSystem(trim(requirements.so()));
        entity.setProcessor(trim(requirements.procesador()));
        entity.setMemory(trim(requirements.memoria()));
        entity.setGraphics(trim(requirements.graficos()));
        entity.setDirectx(trim(requirements.directx()));
        entity.setStorage(trim(requirements.almacenamiento()));
        return entity;
    }

    private String trim(String value) {
        return value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private String companyName(ProductEntity product, String role) {
        return product.getCompanies().stream()
                .filter(company -> role.equals(company.getCompanyRole()))
                .map(ProductCompanyEntity::getCompany)
                .map(company -> company.getName())
                .findFirst()
                .orElseThrow(() -> invalidProduct(product, "no tiene compañía con rol " + role));
    }

    private SystemRequirements toRequirements(SystemRequirementsEntity entity) {
        return new SystemRequirements(
                entity.getOperatingSystem(),
                entity.getProcessor(),
                entity.getMemory(),
                entity.getGraphics(),
                entity.getDirectx(),
                entity.getStorage());
    }

    private IllegalStateException invalidProduct(ProductEntity product, String reason) {
        return new IllegalStateException("El producto " + product.getId() + " " + reason + ".");
    }
}
