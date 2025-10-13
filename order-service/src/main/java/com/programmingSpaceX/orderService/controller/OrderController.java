package com.programmingSpaceX.orderService.controller;

import com.programmingSpaceX.orderService.dto.OrderRequest;
import com.programmingSpaceX.orderService.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

  private final  OrderService orderService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    public String placeOrder(@RequestBody OrderRequest request ){
        orderService.placeOrder(request);
        return "Order Place Successfully";
    }

    public String fallbackMethod(OrderRequest request, RuntimeException runtimeException){
        runtimeException.printStackTrace();
        return "Oops! Something went wrong, please order after some time!";

    }
}
