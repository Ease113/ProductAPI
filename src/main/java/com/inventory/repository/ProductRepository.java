package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 동일한 SKU를 사용하는 상품이 존재하는지 확인한다.
     *
     * @param sku 확인할 상품 SKU
     * @return 상품이 존재하면 true
     */
    boolean existsBySku(String sku);
}
