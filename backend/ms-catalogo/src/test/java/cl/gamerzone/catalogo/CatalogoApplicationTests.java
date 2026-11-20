package cl.gamerzone.catalogo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void contextLoads() {
	}

	@Test
	void openApiDocumentsProductRoutesAndBearerAuthentication() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/api/v1/products'].get.summary").value("Listar productos"))
				.andExpect(jsonPath("$.paths['/api/v1/products'].get.responses.200.content['application/json'].schema.$ref")
						.value("#/components/schemas/ProductPageResponse"))
				.andExpect(jsonPath("$.paths['/api/v1/admin/products'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/admin/products'].get.responses.200.content"
						+ "['application/json'].schema.$ref").value("#/components/schemas/AdminProductPageResponse"))
				.andExpect(jsonPath("$.paths['/api/v1/admin/products'].post.requestBody.content['application/json'].schema.$ref")
						.value("#/components/schemas/ProductCreateRequest"))
				.andExpect(jsonPath("$.paths['/api/v1/admin/products'].post.responses.201.content"
						+ "['application/json'].schema.$ref").value("#/components/schemas/AdminProductDetailResponse"))
				.andExpect(jsonPath("$.paths['/api/v1/admin/products'].post.responses.400.content['application/problem+json']")
						.exists())
				.andExpect(jsonPath("$.components.schemas.ProductDetails.properties.modos.type").value("array"))
				.andExpect(jsonPath("$.components.schemas.ProductDetails.properties.modos.maxItems").value(20))
				.andExpect(jsonPath("$.components.schemas.ProductDetails.properties.modos.items.maxLength").value(100))
				.andExpect(jsonPath("$.components.schemas.ProductDetails.properties.idiomas.type").value("array"))
				.andExpect(jsonPath("$.components.schemas.ProductDetails.properties.idiomas.maxItems").value(20))
				.andExpect(jsonPath("$.components.schemas.ProductDetails.properties.idiomas.items.maxLength").value(100))
				.andExpect(jsonPath("$.paths['/api/v1/admin/products'].post.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/admin/products/{id}'].put.responses.500").exists())
				.andExpect(jsonPath("$.paths['/api/v1/admin/products/{id}'].delete").exists())
				.andExpect(jsonPath("$.paths['/api/v1/admin/products/{id}/status'].patch.requestBody.content"
						+ "['application/json'].schema.$ref").value("#/components/schemas/ProductStatusRequest"))
				.andExpect(jsonPath("$.paths['/api/v1/admin/products/{id}/status'].patch.responses.200.content"
						+ "['application/json'].schema.$ref")
						.value("#/components/schemas/AdminProductDetailResponse"))
				.andExpect(jsonPath("$.components.schemas.ProductStatusRequest.properties.status.enum[0]")
						.value("ACTIVE"))
				.andExpect(jsonPath("$.components.schemas.ProductStatusRequest.properties.status.enum[1]")
						.value("ARCHIVED"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
	}
}
