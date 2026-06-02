package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

// 상품 영속성 접근. 커스텀 조회(예: SKU 중복 확인)는 Phase 2에서 필요 시 추가한다.
public interface ProductRepository extends JpaRepository<Product, Long> {
}
