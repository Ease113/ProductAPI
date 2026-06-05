package com.inventory.repository;

import com.inventory.entity.StockLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLogRepository extends JpaRepository<StockLog, Long> {
}
