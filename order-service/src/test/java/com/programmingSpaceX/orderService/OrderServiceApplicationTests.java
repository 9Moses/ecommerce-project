package com.programmingSpaceX.orderService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmingSpaceX.orderService.dto.OrderLineItemDTO;
import com.programmingSpaceX.orderService.dto.OrderRequest;
import com.programmingSpaceX.orderService.model.OrderEntity;
import com.programmingSpaceX.orderService.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> pgvector = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    static  void setPgvector(DynamicPropertyRegistry dynamicPropertyRegistry){
        dynamicPropertyRegistry.add("spring.data.model.uri", pgvector::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", pgvector::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", pgvector::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDb(){
        orderRepository.deleteAll();
    }

    @Test
    @Transactional
	void placeOrder() throws Exception {

        // Arrange
        OrderRequest orderRequest = getOrderRequest();
        String requestJson = objectMapper.writeValueAsString(orderRequest);

        // Act
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order Place Successfully"));

        // Assert → run inside transaction
        List<OrderEntity> orders = orderRepository.findAll();
        Assertions.assertEquals(1, orders.size());

        OrderEntity savedOrder = orders.get(0);
        Assertions.assertEquals(1, savedOrder.getOrderLineItemList().size());
        Assertions.assertEquals("iphone-17-pro",
                savedOrder.getOrderLineItemList().get(0).getSkuCode());
	}

    private OrderRequest getOrderRequest() {
        OrderLineItemDTO orderLineItemDTO = new OrderLineItemDTO();
        orderLineItemDTO.setSkuCode("iphone-17-pro");
        orderLineItemDTO.setPrice(BigDecimal.valueOf(123000));
        orderLineItemDTO.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrderLineItemList(List.of(orderLineItemDTO));

        return orderRequest;

    }


}
