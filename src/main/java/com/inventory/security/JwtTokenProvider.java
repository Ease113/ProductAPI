package com.inventory.security;

import com.inventory.entity.Role;
import com.inventory.exception.BusinessException;
import com.inventory.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Access Token의 생성과 검증을 담당하는 컴포넌트입니다.
 *
 * <p>이 클래스는 로그인 성공 시 이메일과 권한 정보를 담은 JWT를 발급하고,
 * 이후 요청에서 전달된 JWT의 서명, 만료 시간, claim 정보를 검증합니다.
 * 비밀번호나 민감 정보는 JWT payload에 포함하지 않습니다.</p>
 */
@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    /**
     * JWT 설정값을 기반으로 토큰 서명에 사용할 SecretKey를 생성합니다.
     *
     * <p>JJWT의 HMAC 서명은 문자열 secret을 그대로 쓰지 않고,
     * byte 배열을 기반으로 {@link SecretKey}를 생성해서 사용합니다.</p>
     *
     * @param jwtProperties JWT secret과 access token 만료 시간이 바인딩된 설정 객체
     */
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 로그인한 사용자의 이메일과 권한을 담은 Access Token을 생성합니다.
     *
     * <p>JWT subject에는 사용자를 식별할 수 있는 이메일을 저장하고,
     * role claim에는 Spring Security 권한으로 변환할 수 있는 역할명을 저장합니다.</p>
     *
     * @param email 토큰 subject에 저장할 사용자 이메일
     * @param role 토큰 claim에 저장할 사용자 역할
     * @return 서명과 만료 시간이 포함된 JWT Access Token
     */
    public String createAccessToken(String email, Role role) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + jwtProperties.accessTokenExpiration());
        return Jwts.builder()
                .subject(email)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT를 파싱하여 claim 정보를 반환합니다.
     *
     * <p>이 과정에서 토큰의 서명과 만료 시간이 함께 검증됩니다.
     * 만료된 토큰은 {@code TOKEN_EXPIRED}, 형식이 잘못되었거나 서명이 유효하지 않은 토큰은
     * {@code INVALID_TOKEN}으로 변환합니다.</p>
     *
     * @param token 검증할 JWT 문자열
     * @return 검증된 JWT payload claims
     * @throws BusinessException 토큰이 만료되었거나 유효하지 않은 경우
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT subject에서 사용자 이메일을 추출합니다.
     *
     * <p>필터에서 인증 객체를 만들 때 principal 값으로 사용할 수 있습니다.</p>
     *
     * @param token 이메일을 추출할 JWT 문자열
     * @return JWT subject에 저장된 사용자 이메일
     */
    public String getEmail(String token) {
        return getEmail(parseClaims(token));
    }

    /**
     * 검증된 claim에서 사용자 이메일을 추출한다.
     *
     * @param claims 서명과 만료 검증을 통과한 JWT claims
     * @return JWT subject에 저장된 사용자 이메일
     * @throws BusinessException subject가 비어 있는 경우
     */
    public String getEmail(Claims claims) {
        String email = claims.getSubject();
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return email;
    }

    /**
     * JWT의 role claim을 {@link Role} enum으로 변환합니다.
     *
     * <p>role claim이 없거나 프로젝트에서 정의하지 않은 값이면 유효하지 않은 토큰으로 처리합니다.</p>
     *
     * @param token 역할 정보를 추출할 JWT 문자열
     * @return 토큰에 저장된 사용자 역할
     * @throws BusinessException role claim이 없거나 잘못된 경우
     */
    public Role getRole(String token) {
        return getRole(parseClaims(token));
    }

    /**
     * 검증된 claim의 role 값을 프로젝트 역할 enum으로 변환한다.
     *
     * @param claims 서명과 만료 검증을 통과한 JWT claims
     * @return 토큰에 저장된 사용자 역할
     * @throws BusinessException role claim이 없거나 잘못된 경우
     */
    public Role getRole(Claims claims) {
        String role = claims.get(ROLE_CLAIM, String.class);
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT가 만료되기까지 남은 시간을 밀리초 단위로 계산합니다.
     *
     * <p>로그아웃 토큰을 Redis 블랙리스트에 저장할 때 TTL 값으로 사용할 수 있습니다.
     * 이미 만료된 경우 음수가 되지 않도록 0을 반환합니다.</p>
     *
     * @param token 남은 만료 시간을 계산할 JWT 문자열
     * @return 만료까지 남은 시간, 단위는 millisecond
     */
    public long getRemainingExpireTime(String token) {
        long remaining = parseClaims(token)
                .getExpiration()
                .getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    /**
     * Access Token의 전체 만료 시간을 초 단위로 반환합니다.
     *
     * <p>설정값은 밀리초 단위로 관리하지만, 로그인 응답의 {@code expiresIn}은
     * 일반적으로 초 단위로 내려주는 관례가 있어 1000으로 나누어 반환합니다.</p>
     *
     * @return Access Token 만료 시간, 단위는 second
     */
    public long getAccessTokenExpiration() {
        return jwtProperties.accessTokenExpiration() / 1000;
    }
}
