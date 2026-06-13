package com.inventory.repository;

import com.inventory.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 동일한 이메일을 사용하는 회원이 존재하는지 확인한다.
     *
     * @param email 확인할 이메일
     * @return 회원이 존재하면 true
     */
    boolean existsByEmail(String email);

    /**
     * 이메일로 회원을 조회한다.
     *
     * @param email 조회할 이메일
     * @return 조회된 회원 또는 빈 Optional
     */
    Optional<Member> findByEmail(String email);

}
