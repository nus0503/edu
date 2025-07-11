package com.company.edu.repository.user;

import com.company.edu.entity.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 쿼리 메서드
    Member findMemberByEmail(String email);

    Optional<Member> findByEmail(String email);
}
