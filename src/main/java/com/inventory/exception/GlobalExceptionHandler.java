package com.inventory.exception;

import com.inventory.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 → 표준 {@link ApiResponse} 에러 봉투 변환기.
 *
 * <p>모든 컨트롤러에서 발생한 예외를 한곳에서 받아, 어떤 경로로 실패하든 응답 형태가
 * 일관되도록 한다. (설계 Error Paths) 처리 우선순위는 구체 타입 → 일반 타입 순이다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 — ErrorCode에 매핑된 status/코드로 응답.
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode ec = e.getErrorCode();
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.error(ec.getCode(), e.getMessage()));
    }

    // @Valid 바인딩 실패 — 첫 필드 오류를 요약해 400으로 변환.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT.getCode(), message));
    }

    // 미처리 예외 — 상세는 로그로만 남기고, 응답엔 내부 정보 없는 일반 메시지를 준다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.error(ec.getCode(), ec.getMessage()));
    }
}
