package com.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI InventoryOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("재고관리 API").version("v0.1")
                .description("상품/재고/입출고 관리 Rest API"));
    }
}
