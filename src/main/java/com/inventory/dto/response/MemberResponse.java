package com.inventory.dto.response;

import com.inventory.entity.Member;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String role,
        LocalDateTime createdAt
) {
    /**
     * 회원 엔티티를 비밀번호가 제외된 응답 DTO로 변환한다.
     *
     * @param member 변환할 회원 엔티티
     * @return 회원 응답 DTO
     */
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getRole().name(),
                member.getCreatedAt()
        );
    }
}
