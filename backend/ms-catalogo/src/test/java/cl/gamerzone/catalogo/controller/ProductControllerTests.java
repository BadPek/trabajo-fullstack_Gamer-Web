package cl.gamerzone.catalogo.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.UUID;

import cl.gamerzone.catalogo.config.SecurityConfig;
import cl.gamerzone.catalogo.dto.product.AdminProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.AdminProductPageResponse;
import cl.gamerzone.catalogo.dto.product.AdminProductResponse;
import cl.gamerzone.catalogo.dto.product.ProductDetailResponse;
import cl.gamerzone.catalogo.dto.product.ProductPageResponse;
import cl.gamerzone.catalogo.dto.product.ProductCreateRequest;
import cl.gamerzone.catalogo.dto.product.ProductStatus;
import cl.gamerzone.catalogo.service.ProductService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void listingUsesContractPaginationDefaults() throws Exception {
        when(productService.findAll(0, 20)).thenReturn(new ProductPageResponse(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(productService).findAll(0, 20);
    }

    @Test
    void rejectsPageSizeAboveTheContractMaximum() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void rejectsNegativePageNumbers() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void adminListingRequiresAdministratorAndAppliesStatusFilter() throws Exception {
        var administrator = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
        when(productService.findAdmin(0, 20, ProductStatus.ARCHIVED))
                .thenReturn(new AdminProductPageResponse(
                        List.of(new AdminProductResponse(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                ProductStatus.ARCHIVED,
                                "Elden Ring",
                                59990,
                                "Action RPG",
                                "src/img/elden_ring.jpg",
                                "Descripción breve")),
                        0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/admin/products").param("status", "ARCHIVED"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/admin/products").with(administrator)
                        .param("status", "ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ARCHIVED"));

        verify(productService).findAdmin(0, 20, ProductStatus.ARCHIVED);
    }

    @Test
    void rejectsUnknownAdminStatusFilter() throws Exception {
        var administrator = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));

        mockMvc.perform(get("/api/v1/admin/products").with(administrator).param("status", "DELETED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsUnknownProductStatusInPatchBody() throws Exception {
        var administrator = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));

        mockMvc.perform(patch("/api/v1/admin/products/550e8400-e29b-41d4-a716-446655440000/status")
                        .with(administrator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELETED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void createsProductsAtTheAdministrativeLocation() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        var administrator = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
        when(productService.create(any(ProductCreateRequest.class))).thenReturn(new AdminProductDetailResponse(
                id, ProductStatus.ACTIVE, "Elden Ring", 59990, "Action RPG", "src/img/elden_ring.jpg",
                "Descripción breve", "Descripción", null, List.of("src/img/elden_ring.jpg"), null, null));
        String request = """
                {
                  "titulo": "Elden Ring",
                  "precio": 59990,
                  "categoria": "Action RPG",
                  "imagen": "src/img/elden_ring.jpg",
                  "descripcionCorta": "Descripción breve",
                  "descripcion": "Descripción",
                  "video": null,
                  "galeria": ["src/img/elden_ring.jpg"],
                  "detalles": {
                    "desarrollador": "FromSoftware",
                    "editor": "Bandai Namco",
                    "fechaLanzamiento": "2022-02-25",
                    "modos": "Un jugador",
                    "idiomas": "Español"
                  },
                  "requisitos": {
                    "minimos": {
                      "so": "Windows",
                      "procesador": "CPU",
                      "memoria": "8 GB",
                      "graficos": "GPU",
                      "directx": "12",
                      "almacenamiento": "60 GB"
                    },
                    "recomendados": {
                      "so": "Windows",
                      "procesador": "CPU",
                      "memoria": "16 GB",
                      "graficos": "GPU",
                      "directx": "12",
                      "almacenamiento": "60 GB"
                    }
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/admin/products").with(administrator)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/admin/products/" + id));
    }

    @Test
    void productIdentifiersUseContractUuidFormat() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        when(productService.findById(id)).thenReturn(new ProductDetailResponse(
                id, "Elden Ring", 59990, "Action RPG", "src/img/elden_ring.jpg",
                "Descripción breve", "Descripción", null, List.of("src/img/elden_ring.jpg"), null, null));

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(productService).findById(id);
    }

    @Test
    void rejectsMalformedProductIdentifiers() throws Exception {
        mockMvc.perform(get("/api/v1/products/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsAdministrativeWritesWithoutAToken() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
        mockMvc.perform(get("/api/v1/admin/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsInvalidBearerTokens() throws Exception {
        when(jwtDecoder.decode("invalid")).thenThrow(new BadJwtException("Invalid JWT"));

        mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void deniesClientAuthorityOnProductAdministration() throws Exception {
        var client = jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));

        mockMvc.perform(post("/api/v1/admin/products").with(client)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(get("/api/v1/admin/products").with(client))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/products/550e8400-e29b-41d4-a716-446655440000").with(client))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/admin/products/550e8400-e29b-41d4-a716-446655440000")
                        .with(client).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/admin/products/550e8400-e29b-41d4-a716-446655440000").with(client))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/admin/products/550e8400-e29b-41d4-a716-446655440000/status")
                        .with(client).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdministratorAuthenticationToReachRequestValidation() throws Exception {
        var administrator = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));

        mockMvc.perform(post("/api/v1/admin/products")
                        .with(administrator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(patch("/api/v1/admin/products/550e8400-e29b-41d4-a716-446655440000/status")
                        .with(administrator)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void oldPublicProductWritePathIsNotAnAdministrativeAlias() throws Exception {
        var administrator = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));

        mockMvc.perform(post("/api/v1/products").with(administrator)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
