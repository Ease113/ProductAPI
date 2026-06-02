package com.inventory.exception;

import lombok.Getter;

/**
 * 도메인/비즈니스 규칙 위반을 표현하는 단일 예외.
 *
 * <p>예외 종류별 클래스를 만드는 대신 {@link ErrorCode}를 들고 다니는 하나의 예외로 통일했다.
 * (설계 Decision 2) 서비스 계층에서 {@code throw new BusinessException(RESOURCE_NOT_FOUND)}
 * 처럼 던지면 {@link GlobalExceptionHandler}가 status/코드로 변환한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 메시지(ErrorCode에 정의된) 사용.
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 상황별 상세 메시지로 기본 메시지를 덮어쓸 때 사용(예: 어떤 id가 없는지).
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
