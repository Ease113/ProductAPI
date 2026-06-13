package com.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Swagger UI와 OpenAPI 명세에 표시할 API 기본 정보를 구성한다.
     *
     * <p>Swagger UI의 Authorize 기능에서 JWT를 입력할 수 있도록
     * HTTP Bearer 보안 스키마를 함께 등록한다.</p>
     *
     * @return 기본 정보와 JWT Bearer 스키마가 설정된 OpenAPI 객체
     */
    @Bean
    public OpenAPI InventoryOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme))
                .info(new Info()
                        .title("재고관리 API").version("v0.1")
                        .description("상품/재고/입출고 관리 Rest API"));
    }
}
