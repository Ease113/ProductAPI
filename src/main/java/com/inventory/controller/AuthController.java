package com.inventory.controller;

import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.SignupRequest;
import com.inventory.dto.response.ApiResponse;
import com.inventory.dto.response.MemberResponse;
import com.inventory.dto.response.TokenResponse;
import com.inventory.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "인증 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    /**
     * 신규 회원을 USER 역할로 등록한다.
     *
     * @param request 검증된 회원가입 요청
     * @return 생성된 회원 정보가 담긴 공통 응답
     */
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    /**
     * 이메일과 비밀번호를 검증하고 JWT Access Token을 발급한다.
     *
     * @param request 검증된 로그인 요청
     * @return 발급된 Access Token 정보가 담긴 공통 응답
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 JWT Access Token을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    /**
     * 현재 Bearer Access Token을 로그아웃 블랙리스트에 등록한다.
     *
     * <p>보안 필터가 헤더 형식과 토큰을 먼저 검증하므로, 컨트롤러는 Bearer 접두사를
     * 제거한 토큰을 서비스에 전달한다.</p>
     *
     * @param authorization Bearer JWT가 포함된 Authorization 헤더
     * @return 데이터가 없는 로그아웃 성공 응답
     */
    @Operation(summary = "로그아웃", description = "현재 JWT Access Token을 즉시 사용할 수 없게 처리합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization.substring(BEARER_PREFIX.length()));
        return ApiResponse.success(null);
    }
}
