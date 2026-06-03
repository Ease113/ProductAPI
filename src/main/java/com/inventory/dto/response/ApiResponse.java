package com.inventory.dto.response;


public record ApiResponse<T> (boolean success, T data, ErrorBody error) {
    // API 응답 성공 시 Response
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    // API 응답 실패 시 Response
    // 실패의 경우 응답 데이터가 없으므로 T제네릭은 void 대입
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }
    // Error를 담을 ErrorBody -> 추후 공통 컴포넌트로 사용 가능
    public record ErrorBody(String code, String message) {

    }
}
