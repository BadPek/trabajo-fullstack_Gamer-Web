package cl.gamerzone.catalogo.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import cl.gamerzone.catalogo.dto.product.AdminProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.AdminProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.ProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductStatus;
import cl.gamerzone.catalogo.dto.product.ProductStatusRequest;
import cl.gamerzone.catalogo.dto.product.ProductUpdateRequest;
import cl.gamerzone.catalogo.exception.ApiError;
import cl.gamerzone.catalogo.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Productos", description = "Catálogo público y administración de productos.")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    @Operation(
            summary = "Listar productos",
            description = "Lectura pública. La respuesta contractual está definida, pero la operación no estará "
                    + "disponible hasta integrar la persistencia del modelo relacional aprobado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Respuesta contractual prevista; todavía no está disponible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ProductPageResponse findAll(
            @Parameter(description = "Índice de página desde cero.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Cantidad de productos por página (máximo 100).", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return productService.findAll(page, size);
    }

    @GetMapping("/products/{id}")
    @Operation(
            summary = "Consultar producto por ID",
            description = "Lectura pública. Requiere integrar la persistencia antes de estar disponible.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Respuesta contractual prevista; todavía no está disponible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "El ID no es válido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ProductDetailResponse findById(
            @Parameter(description = "Identificador UUID del producto.",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return productService.findById(id);
    }

    @GetMapping("/admin/products")
    @Operation(
            summary = "Listar productos para administración",
            description = "Requiere ADMINISTRADOR. Permite filtrar por estado y usa paginación contractual.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos, incluido su estado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminProductPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de consulta inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public AdminProductPageResponse findAdmin(
            @Parameter(description = "Índice de página desde cero.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Cantidad de productos por página (máximo 100).", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Filtrar por estado del producto.", example = "ACTIVE")
            @RequestParam(required = false) ProductStatus status) {
        return productService.findAdmin(page, size, status);
    }

    @GetMapping("/admin/products/{id}")
    @Operation(
            summary = "Consultar producto para administración",
            description = "Incluye productos archivados. Requiere ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminProductDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "El ID no es válido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public AdminProductDetailResponse findAdminById(
            @Parameter(description = "Identificador UUID del producto.",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return productService.findAdminById(id);
    }

    @PostMapping("/admin/products")
    @Operation(
            summary = "Crear producto",
            description = "Requiere ADMINISTRADOR. La operación no estará disponible hasta integrar la persistencia "
                    + "del modelo relacional aprobado.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Respuesta contractual prevista; todavía no está disponible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminProductDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "415", description = "Tipo de contenido no soportado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AdminProductDetailResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos completos del producto.",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductCreateRequest.class)))
            @Valid @RequestBody ProductCreateRequest request) {
        AdminProductDetailResponse created = productService.create(request);
        URI location = URI.create("/api/v1/admin/products/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/admin/products/{id}")
    @Operation(
            summary = "Actualizar producto completamente",
            description = "Requiere ADMINISTRADOR. La operación no estará disponible hasta integrar la persistencia "
                    + "del modelo relacional aprobado.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Respuesta contractual prevista; todavía no está disponible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminProductDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID o solicitud inválidos.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "415", description = "Tipo de contenido no soportado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public AdminProductDetailResponse update(
            @Parameter(description = "Identificador UUID del producto.",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Representación completa del producto actualizado.",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductUpdateRequest.class)))
            @Valid @RequestBody ProductUpdateRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/admin/products/{id}")
    @Operation(
            summary = "Archivar producto",
            description = "Requiere ADMINISTRADOR. Cuando exista persistencia, implementará archivado lógico. "
                    + "Actualmente no está disponible.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Respuesta contractual prevista; todavía no está disponible."),
            @ApiResponse(responseCode = "400", description = "ID inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> archive(
            @Parameter(description = "Identificador UUID del producto.",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        productService.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/products/{id}/status")
    @Operation(
            summary = "Cambiar estado del producto",
            description = "Activa o archiva el producto. Requiere ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdminProductDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID o estado inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMINISTRADOR.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Persistencia pendiente.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public AdminProductDetailResponse updateStatus(
            @Parameter(description = "Identificador UUID del producto.",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Estado explícito ACTIVE o ARCHIVED.",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductStatusRequest.class)))
            @Valid @RequestBody ProductStatusRequest request) {
        return productService.updateStatus(id, request.status());
    }
}
