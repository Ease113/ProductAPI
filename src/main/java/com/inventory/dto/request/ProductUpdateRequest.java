package com.inventory.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자 이하로 입력해주세요.")
        String name,

        @NotNull(message = "가격은 필수입니다.")
        @Positive(message = "가격은 0보다 커야합니다.")
        @Digits(integer = 10, fraction = 2, message = "가격은 최대 10자리의 정수와 2자리의 소수로 입력해주세요.")
        BigDecimal price,

        @NotBlank(message = "카테고리는 필수입니다.")
        @Size(max = 50, message = "카테고리는 50자 이하로 입력해주세요.")
        String category
) {
}
