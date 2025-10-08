package com.programmingSpaceX.invertoryService.Repository;

import com.programmingSpaceX.invertoryService.model.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    List<InventoryEntity> findBySkuCodeIn(List<String> skuCode);
}
