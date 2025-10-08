package com.programmingSpaceX.product_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmingSpaceX.product_service.DTO.ProductRequest;
import com.programmingSpaceX.product_service.model.ProductEntity;
import com.programmingSpaceX.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository repository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll(); // ✅ clears DB before each test
    }

	@Test
	void shouldCreateProduct() throws Exception {
       ProductRequest productRequest = getProductRequest();

        String productRequestString = objectMapper.writeValueAsString(productRequest);


        mockMvc.perform(MockMvcRequestBuilders.post("/api/product/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productRequestString))
                .andExpect(status().isCreated());

        Assertions.assertEquals(1, repository.findAll().size());
	}

    @Test
    void shouldGetAllProduct() throws Exception {

        // Arrange → clear DB, then insert exactly 1 product
        repository.deleteAll();
        repository.save(ProductEntity.builder()
                .name("Iphone 17 Pro")
                .description("iOS 27B")
                .price(BigDecimal.valueOf(123000))
                .build());

        // Act + Assert → fetch all
        mockMvc.perform(MockMvcRequestBuilders.get("/api/product/getAllProduct")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // ✅ exactly 1 product
                .andExpect(jsonPath("$[0].name").value("Iphone 17 Pro"))
                .andExpect(jsonPath("$[0].description").value("iOS 27B"))
                .andExpect(jsonPath("$[0].price").value(123000));
    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("Iphone 17 Pro")
                .description("iOS 27B")
                .price(BigDecimal.valueOf(123000))
                .build();
    }

}
