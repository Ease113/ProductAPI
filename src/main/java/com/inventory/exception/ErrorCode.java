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
    PRODUCT_NOT_FOUND (HttpStatus.NOT_FOUND, "P001", "요청한 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
