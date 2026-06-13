package com.inventory.service;

import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.SignupRequest;
import com.inventory.dto.response.MemberResponse;
import com.inventory.dto.response.TokenResponse;
import com.inventory.entity.Member;
import com.inventory.entity.Role;
import com.inventory.exception.BusinessException;
import com.inventory.exception.ErrorCode;
import com.inventory.repository.MemberRepository;
import com.inventory.security.JwtTokenProvider;
import com.inventory.security.JwtBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklistService jwtBlacklistService;

    /**
     * 비밀번호 정책과 이메일 중복을 검증한 뒤 USER 역할의 회원을 등록한다.
     *
     * @param request 회원가입 요청
     * @return 비밀번호가 제외된 가입 회원 정보
     * @throws BusinessException 비밀번호 정책을 위반하거나 이메일이 중복된 경우
     */
    @Transactional
    public MemberResponse signup(SignupRequest request) {
        validatePasswordPolicy(request.password());

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    /**
     * 이메일과 비밀번호를 검증하고 JWT Access Token을 발급한다.
     *
     * @param request 로그인 요청
     * @return Access Token과 만료 정보
     * @throws BusinessException 회원이 없거나 비밀번호가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getEmail(),
                member.getRole()
        );

        return new TokenResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    /**
     * Access Token을 남은 만료 시간 동안 Redis 블랙리스트에 등록한다.
     *
     * @param token 로그아웃할 JWT Access Token
     * @throws BusinessException 토큰 검증 또는 Redis 저장에 실패한 경우
     */
    public void logout(String token) {
        long remainingExpireTime = jwtTokenProvider.getRemainingExpireTime(token);
        jwtBlacklistService.add(token, remainingExpireTime);
    }

    /**
     * 비밀번호에 대문자와 특수문자가 각각 포함되어 있는지 검증한다.
     *
     * @param password 검증할 평문 비밀번호
     * @throws BusinessException 비밀번호 정책을 충족하지 못한 경우
     */
    private void validatePasswordPolicy(String password) {
        // 비밀번호 대문자 보유 여부 체크
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        // 비밀번호 특수문자 보유 여부 체크
        boolean hasSpecialChar = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:'\",.<>?/".indexOf(ch) >= 0);

        if (!hasUppercase || !hasSpecialChar) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_POLICY);
        }
    }
}
