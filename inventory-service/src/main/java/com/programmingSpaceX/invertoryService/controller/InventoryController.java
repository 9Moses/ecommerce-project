package com.programmingSpaceX.invertoryService.controller;

import com.programmingSpaceX.invertoryService.Service.InventoryService;
import com.programmingSpaceX.invertoryService.dto.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    //http://localhost:8082/api/inventory?skuCode=iphone13&skuCode=iphone14
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode){

        return  inventoryService.isInStock(skuCode);
    }
}
