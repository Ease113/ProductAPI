package com.inventory.dto.response;

import com.inventory.entity.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        String category
) {
    /**
     * 상품 엔티티를 외부 응답 DTO로 변환한다.
     *
     * @param product 변환할 상품 엔티티
     * @return 상품 응답 DTO
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getCategory()
        );
    }
}
