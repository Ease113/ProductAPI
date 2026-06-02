package com.inventory.repository;

import com.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

// 재고 영속성 접근. 입출고 시 비관적 락 조회 등은 Phase 5·6에서 추가한다.
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
