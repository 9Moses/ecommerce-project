package com.programmingSpaceX.invertoryService.Service;

import brave.Tracer;
import com.programmingSpaceX.invertoryService.Repository.InventoryRepository;
import com.programmingSpaceX.invertoryService.dto.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode){
        //log.info("Wait Started");
//        try {
//            Thread.sleep(10000); // simulate delay
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // restore interrupted status
//            log.error("Thread was interrupted while sleeping", e);
//        }
//        log.info("Wait Ended");
      return inventoryRepository.findBySkuCodeIn(skuCode).stream()
              .map(inventory ->
                  InventoryResponse.builder()
                          .skucode(inventory.getSkuCode())
                          .isInStock(inventory.getQuantity() > 0)
                          .build()
              )
              .toList();
    }

    private final Tracer tracer;

    public InventoryService(InventoryRepository inventoryRepository, Tracer tracer) {
        this.inventoryRepository = inventoryRepository;
        this.tracer = tracer;
    }

    public void testTrace() {
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            System.out.println("Current trace ID: " + currentSpan.context().traceId());
            System.out.println("Current span ID: " + currentSpan.context().spanId());
        } else {
            System.out.println("⚠️ No current span found — tracing not active!");
        }
    }
}
