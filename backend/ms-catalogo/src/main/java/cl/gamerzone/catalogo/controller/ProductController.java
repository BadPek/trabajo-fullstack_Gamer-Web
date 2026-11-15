package cl.gamerzone.catalogo.controller;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.ProductPageResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Productos", description = "Catálogo público y administración de productos.")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
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

    @GetMapping("/{id}")
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
            @Parameter(description = "Identificador del producto.", example = "1")
            @PathVariable @Positive Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Crear producto",
            description = "Requiere ADMINISTRADOR. La operación no estará disponible hasta integrar la persistencia "
                    + "del modelo relacional aprobado.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Respuesta contractual prevista; todavía no está disponible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDetailResponse.class))),
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
    public ResponseEntity<ProductDetailResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos completos del producto.",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductCreateRequest.class)))
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse created = productService.create(request);
        URI location = URI.create("/api/v1/products/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar producto completamente",
            description = "Requiere ADMINISTRADOR. La operación no estará disponible hasta integrar la persistencia "
                    + "del modelo relacional aprobado.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Respuesta contractual prevista; todavía no está disponible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDetailResponse.class))),
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
    public ProductDetailResponse update(
            @Parameter(description = "Identificador del producto.", example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Representación completa del producto actualizado.",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductUpdateRequest.class)))
            @Valid @RequestBody ProductUpdateRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
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
            @Parameter(description = "Identificador del producto.", example = "1")
            @PathVariable @Positive Long id) {
        productService.archive(id);
        return ResponseEntity.noContent().build();
    }
}
