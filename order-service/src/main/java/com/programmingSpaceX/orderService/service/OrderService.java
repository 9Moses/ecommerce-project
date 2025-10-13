package com.programmingSpaceX.orderService.service;

import com.programmingSpaceX.orderService.dto.InventoryResponse;
import com.programmingSpaceX.orderService.dto.OrderRequest;
import com.programmingSpaceX.orderService.dto.OrderLineItemDTO;
import com.programmingSpaceX.orderService.model.OrderEntity;
import com.programmingSpaceX.orderService.model.OrderLineItem;
import com.programmingSpaceX.orderService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest request){
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


      //Call Inventory service, and place order if product is in stock
       InventoryResponse[] inventoryResponsesArray =  webClientBuilder.build().get()
                .uri("http://INVENTORYSERVICE/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build() )
                        .retrieve()
                                .bodyToMono(InventoryResponse[].class)
                                        .block();


        boolean allProductIsInStock = Arrays.stream(inventoryResponsesArray)
                .allMatch(InventoryResponse::isInStock);

       if(allProductIsInStock){
           repository.save(order);
       }else{
           throw new IllegalArgumentException("Product is not in stock, please try again later");
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
