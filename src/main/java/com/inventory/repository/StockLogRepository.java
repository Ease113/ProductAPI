package com.inventory.repository;

import com.inventory.entity.StockLog;
import org.springframework.data.jpa.repository.JpaRepository;

// 입출고 이력 영속성 접근. 상품별 이력 조회 등은 Phase 5에서 추가한다.
public interface StockLogRepository extends JpaRepository<StockLog, Long> {
}
