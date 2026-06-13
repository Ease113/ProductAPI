package com.inventory.common.cache;

import com.inventory.dto.response.ProductResponse;

public final class CacheNames {

    public static final String PRODUCTS = "products";
    public static final String PRODUCT_DETAIL = "productDetails";

    /**
     * 상수 전용 클래스의 인스턴스 생성을 막는다.
     */
    private CacheNames() {
    }
}
