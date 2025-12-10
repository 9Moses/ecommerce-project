package com.programmingSpaceX.orderService.service;


import com.programmingSpaceX.orderService.dto.InventoryResponse;
import com.programmingSpaceX.orderService.dto.OrderRequest;
import com.programmingSpaceX.orderService.dto.OrderLineItemDTO;
import com.programmingSpaceX.orderService.event.OrderPlaceEvent;
import com.programmingSpaceX.orderService.model.OrderEntity;
import com.programmingSpaceX.orderService.model.OrderLineItem;
import com.programmingSpaceX.orderService.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    private final Tracer tracer;

    private final KafkaTemplate<String, OrderPlaceEvent> KafkaTemplate;


    public String placeOrder(OrderRequest request){
        OrderEntity order = new OrderEntity();
        order.setOrderNumber(UUID.randomUUID().toString());

      List<OrderLineItem> orderLineItemList = request.getOrderLineItemList()
                .stream()
                .map(this::mapToDTO)
                .toList();

      order.setOrderLineItemList(orderLineItemList);

      List<String> skuCodes =   order.getOrderLineItemList().stream()
              .map(OrderLineItem::getSkuCode)
              .toList();


      Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        try (Tracer.SpanInScope ws = tracer.withSpan(inventoryServiceLookup.start())) {

            // Call Inventory service
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("http://inventory-service/api/inventory");
                        skuCodes.forEach(sku -> uriBuilder.queryParam("skuCode", sku));
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .timeout(Duration.ofSeconds(5)) // don't wait forever
                    .onErrorResume(ex -> {
                        // fallback if inventory service is down
                        throw new IllegalStateException("Inventory service unavailable, please try again later", ex);
                    })
                    .block();
            System.out.println("Calling Inventory Service with SKUs: " + skuCodes);

            assert inventoryResponseArray != null;
            boolean allProductIsInStock = Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);

            if (allProductIsInStock) {
                repository.save(order);
                KafkaTemplate.send("notificationTopic", new OrderPlaceEvent(order.getOrderNumber()));
                return "Order Placed Successfully";
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }

        } finally {
            inventoryServiceLookup.end();
        }
    }

    private OrderLineItem mapToDTO(OrderLineItemDTO orderLineItemDTO) {
            OrderLineItem orderLineItem = new OrderLineItem();
            orderLineItem.setPrice(orderLineItemDTO.getPrice());
            orderLineItem.setQuantity(orderLineItemDTO.getQuantity());
            orderLineItem.setSkuCode(orderLineItemDTO.getSkuCode());

            return orderLineItem;

    }
}
