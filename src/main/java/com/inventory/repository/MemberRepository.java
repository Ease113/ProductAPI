package com.inventory.repository;

import com.inventory.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// 회원 영속성 접근. 이메일 기반 조회(findByEmail)는 Phase 4 인증 단계에서 추가한다.
public interface MemberRepository extends JpaRepository<Member, Long> {
}
