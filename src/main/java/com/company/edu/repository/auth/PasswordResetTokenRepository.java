package com.company.edu.repository.auth;

import com.company.edu.entity.auth.PasswordResetToken;
import com.company.edu.entity.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByMember(Member member);
}
