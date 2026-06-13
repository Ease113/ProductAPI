package com.inventory.dto.response;


public record ApiResponse<T> (boolean success, T data, ErrorBody error) {

    /**
     * 성공 데이터를 공통 API 응답 형식으로 감싼다.
     *
     * @param data 응답에 포함할 데이터
     * @param <T> 응답 데이터 타입
     * @return 성공 상태의 API 응답
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    /**
     * 오류 코드와 메시지를 공통 API 응답 형식으로 감싼다.
     *
     * @param code 애플리케이션 오류 코드
     * @param message 사용자에게 전달할 오류 메시지
     * @return 실패 상태의 API 응답
     */
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }
    // Error를 담을 ErrorBody -> 추후 공통 컴포넌트로 사용 가능
    public record ErrorBody(String code, String message) {

    }
}
