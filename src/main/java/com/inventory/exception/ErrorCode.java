package com.inventory.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 오류 카탈로그.
 *
 * <p>HTTP status·안정적 코드 문자열·기본 메시지를 한 곳에 모아, 새 오류는 상수만 추가하면
 * 되도록 했다. (설계 Decision 2) 코드 문자열(C001…)은 응답에 노출되어 클라이언트 분기 키가
 * 되므로, 한 번 부여하면 바꾸지 않는다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "C003", "이미 존재하는 리소스입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
