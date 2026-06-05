package com.inventory.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ⚠️ 임시 보안 설정 — Phase 4에서 JWT 인증/인가로 정식 교체된다. (설계 Decision 3)
 *
 * <p>spring-boot-starter-security가 클래스패스에 있으면 기본값으로 모든 요청이 401이 되어
 * Swagger조차 열리지 않는다. Phase 1~3 동안 개발·문서 확인이 가능하도록 전부 permitAll로
 * 열어둔다. 의존성을 뺐다 넣는 대신 이 설정 Bean 하나만 두어, Phase 4에서 이 파일만
 * 정식 규칙으로 바꾸면 변경이 국소적으로 끝나도록 한 의도다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 임시: REST API라 세션/CSRF 토큰을 쓰지 않으므로 CSRF 비활성화 + 전체 허용.
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
