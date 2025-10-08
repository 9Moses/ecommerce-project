package com.programmingSpaceX.orderService.controller;

import com.programmingSpaceX.orderService.dto.OrderRequest;
import com.programmingSpaceX.orderService.service.OrderService;
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
    public String placeOrder(@RequestBody OrderRequest request ){
        orderService.placeOrder(request);
        return "Order Place Successfully";
    }
}
