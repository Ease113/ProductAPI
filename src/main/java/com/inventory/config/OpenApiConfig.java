package com.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI(/swagger-ui.html)에 노출될 API 문서 메타데이터.
 *
 * 엔드포인트 자체는 Phase 2부터 컨트롤러가 추가되며 자동 수집되고, 여기서는 제목/버전 등
 * 문서 상단 정보만 정의한다.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("재고관리 API")
                .version("v0.1")
                .description("상품·재고·입출고 관리 REST API"));
    }
}
