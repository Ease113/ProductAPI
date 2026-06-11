package com.inventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        String email,
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(message = "비밀번호는 8자 20자 이하로 입력해주세요.", min = 8, max = 20)
        String password
) {
}
