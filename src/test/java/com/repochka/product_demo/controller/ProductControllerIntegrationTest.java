package com.repochka.product_demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repochka.product_demo.TestcontainersConfiguration;
import com.repochka.product_demo.dto.ProductCreateRequest;
import com.repochka.product_demo.dto.ProductUpdateRequest;
import com.repochka.product_demo.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_withValidBody_returns201() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
                "Ведьмак. Меч Предназначения",
                "Вторая книга из цикла «Ведьмак», в которую вошли шесть повестей о ведьмаке Геральте.",
                new BigDecimal("39.99"), Category.BOOKS);

        OffsetDateTime before = OffsetDateTime.now();

        String responseBody = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Ведьмак. Меч Предназначения")))
                .andExpect(jsonPath("$.description", is("Вторая книга из цикла «Ведьмак», в которую вошли шесть повестей о ведьмаке Геральте.")))
                .andExpect(jsonPath("$.price", is(39.99)))
                .andExpect(jsonPath("$.category", is("BOOKS")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var json = objectMapper.readTree(responseBody);
        OffsetDateTime createdAt = OffsetDateTime.parse(json.get("createdAt").asText());
        OffsetDateTime updatedAt = OffsetDateTime.parse(json.get("updatedAt").asText());

        assertThat(createdAt).isCloseToUtcNow(within(5, ChronoUnit.SECONDS));
        assertThat(updatedAt).isCloseToUtcNow(within(5, ChronoUnit.SECONDS));
    }

    @Test
    void getProductById_returns200() throws Exception {
        Long id = createTestProduct("Test Product", new BigDecimal("10.00"), Category.ELECTRONICS);

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.price", is(10.00)));
    }

    @Test
    void getProductById_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/products/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Product not found with id: 99999")));
    }

    @Test
    void updateProduct_returns200WithUpdatedFields() throws Exception {
        Long id = createTestProduct("Original", new BigDecimal("10.00"), Category.ELECTRONICS);

        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                "Updated", null, new BigDecimal("20.00"), Category.BOOKS);

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.price", is(20.00)))
                .andExpect(jsonPath("$.category", is("BOOKS")));
    }

    @Test
    void deleteProduct_returns200() throws Exception {
        Long id = createTestProduct("To Delete", new BigDecimal("5.00"), Category.FOOD);

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void getDeletedProduct_returns404() throws Exception {
        Long id = createTestProduct("To Delete", new BigDecimal("5.00"), Category.FOOD);

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_withMissingName_returns400WithFieldErrors() throws Exception {
        String body = """
                {"price": 10.00, "category": "BOOKS"}
                """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors[0].message", notNullValue()));
    }

    @Test
    void createProduct_withNegativePrice_returns400() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
                "Bad Product", null, new BigDecimal("-5.00"), Category.OTHER);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void createProduct_withThreeDecimalPlaces_returns400() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
                "Precise Product", null, new BigDecimal("19.999"), Category.ELECTRONICS);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void deleteAlreadyDeletedProduct_returns404() throws Exception {
        Long id = createTestProduct("Delete Twice", new BigDecimal("5.00"), Category.FOOD);

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    private Long createTestProduct(String name, BigDecimal price, Category category) throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(name, null, price, category);

        String responseBody = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }
}
