package com.programmingSpaceX.invertoryService;

import com.programmingSpaceX.invertoryService.Repository.InventoryRepository;
import com.programmingSpaceX.invertoryService.model.InventoryEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class InvertoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvertoryServiceApplication.class, args);
	}

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository){
        return  args -> {
            InventoryEntity inventory = new InventoryEntity();
            inventory.setSkuCode("iphone_17 Air");
            inventory.setQuantity(100);

            InventoryEntity inventory1 = new InventoryEntity();
            inventory1.setSkuCode("iphone_17 Pro");
            inventory1.setQuantity(10);

            inventoryRepository.save(inventory);
            inventoryRepository.save(inventory1);
        };

    }
}
