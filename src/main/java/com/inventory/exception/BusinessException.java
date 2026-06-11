package com.inventory.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    /**
     * 오류 코드의 기본 메시지를 사용하는 비즈니스 예외를 생성한다.
     *
     * @param errorCode 응답 상태와 메시지를 정의한 오류 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 별도 상세 메시지를 사용하는 비즈니스 예외를 생성한다.
     *
     * @param errorCode 응답 상태를 정의한 오류 코드
     * @param message 예외 상세 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
