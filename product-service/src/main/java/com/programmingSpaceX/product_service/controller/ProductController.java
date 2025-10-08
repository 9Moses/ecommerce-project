package com.programmingSpaceX.product_service.controller;

import com.programmingSpaceX.product_service.DTO.ProductRequest;
import com.programmingSpaceX.product_service.DTO.ProductResponse;
import com.programmingSpaceX.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest request){
        service.createProduct(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProduct(){
       return service.getAllProduct();
    }
}
