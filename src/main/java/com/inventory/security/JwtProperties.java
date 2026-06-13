package com.inventory.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code jwt} 접두사의 토큰 서명 키와 Access Token 만료 시간을 바인딩한다.
 *
 * @param secret HMAC 서명에 사용할 비밀 키
 * @param accessTokenExpiration Access Token 만료 시간(밀리초)
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpiration
) {
}
