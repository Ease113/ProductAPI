package com.inventory.dto.response;

/**
 * 모든 API 응답을 감싸는 공통 봉투.
 *
 * <p>성공·실패 모두 동일한 형태({@code success}/{@code data}/{@code error})로 내려보내
 * 클라이언트가 단일 규약으로 분기할 수 있게 한다. (설계 Decision 1)
 *
 * @param <T> 성공 시 본문 데이터 타입
 */
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {

    // 성공 응답 — error는 비운다.
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 실패 응답 — data는 비우고 오류 코드/메시지를 담는다. (GlobalExceptionHandler에서 사용)
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    // 오류 본문 — code는 ErrorCode의 안정적인 문자열(C001 등), message는 사람이 읽는 설명.
    public record ErrorBody(String code, String message) {
    }
}
