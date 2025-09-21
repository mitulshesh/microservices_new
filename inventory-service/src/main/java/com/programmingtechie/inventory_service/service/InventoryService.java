package com.programmingtechie.inventory_service.service;


import com.programmingtechie.inventory_service.dto.InventoryResponse;
import com.programmingtechie.inventory_service.repository.InventoryRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService{

        private final InventoryRepository inventoryRepository;

        @Transactional
        public List<InventoryResponse> isInStock(List<String> skuCode) throws InterruptedException {
        log.info("Checking Inventory");
        //log.info("Intentional wait started..");
            //Thread.sleep(10000);
        // log.info("Intentional wait ended..");
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList();
    }
}
