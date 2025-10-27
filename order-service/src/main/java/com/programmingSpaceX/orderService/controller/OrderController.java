package com.programmingSpaceX.orderService.controller;

import com.programmingSpaceX.orderService.dto.OrderRequest;
import com.programmingSpaceX.orderService.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

  private final  OrderService orderService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory" )
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest request ){
        return  CompletableFuture.supplyAsync(()-> orderService.placeOrder(request));
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest request, RuntimeException runtimeException){
        runtimeException.printStackTrace();
        return  CompletableFuture.supplyAsync(()-> "Oops! Something went wrong, please order after some time!") ;

    }
}
