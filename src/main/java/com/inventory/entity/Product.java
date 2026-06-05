package com.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // SKU는 상품 고유 식별 코드 — 중복 등록을 막기 위해 유니크 제약을 둔다.
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    // 금액은 부동소수 오차를 피하기 위해 BigDecimal(DECIMAL) 사용
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 50)
    private String category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Product(String name, String sku, BigDecimal price, String category) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.category = category;
    }

    public void update(String name, BigDecimal price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }
}