package com.programmingSpaceX.product_service.service;

import com.programmingSpaceX.product_service.DTO.ProductRequest;
import com.programmingSpaceX.product_service.DTO.ProductResponse;
import com.programmingSpaceX.product_service.model.ProductEntity;
import com.programmingSpaceX.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository repository;

    public void createProduct(ProductRequest request){
        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();

        repository.save(product);
        log.info("Product " + product.getId() + "is saved");
    }

    public List<ProductResponse> getAllProduct(){
        List<ProductEntity> products = repository.findAll();
       return products.stream().map(this::mapToProductResponse).toList();

    }

    private ProductResponse mapToProductResponse(ProductEntity product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}
