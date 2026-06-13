package com.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class InventoryApplication {

    /**
     * 재고 관리 API 애플리케이션을 시작한다.
     *
     * @param args 애플리케이션 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
