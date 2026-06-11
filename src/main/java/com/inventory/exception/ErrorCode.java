package com.inventory.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


/**
 * 에러 코드 enum
 * §@RequireArgsConstructor§가 생성자를 동적 생성
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT (HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND (HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE (HttpStatus.CONFLICT, "C003", "이미 존재하는 리소스입니다."),
    INTERNAL_ERROR (HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 내부 오류가 발생했습니다."),
    PRODUCT_NOT_FOUND (HttpStatus.NOT_FOUND, "P001", "요청한 상품을 찾을 수 없습니다."),
    DUPLICATE_EMAIL (HttpStatus.CONFLICT, "M001", "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS (HttpStatus.UNAUTHORIZED, "M002", "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_PASSWORD_POLICY (HttpStatus.BAD_REQUEST, "M003", "비밀번호는 대문자와 특수문자를 각각 1개 이상 포함해야 합니다."),
    AUTHENTICATION_REQUIRED (HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_TOKEN (HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED (HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    LOGGED_OUT_TOKEN (HttpStatus.UNAUTHORIZED, "A004", "로그아웃된 토큰입니다."),
    ACCESS_DENIED (HttpStatus.FORBIDDEN, "A005", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
