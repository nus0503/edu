package com.company.edu.repository.auth;

import com.company.edu.entity.auth.RefreshToken;
import com.company.edu.entity.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 ID로 활성화된 리프레시 토큰 조회
     */
    Optional<RefreshToken> findByTokenIdAndIsActiveTrue(String tokenId);

    /**
     * 회원 ID로 모든 활성화된 리프레시 토큰 조회
     */
    List<RefreshToken> findByMemberAndIsActiveTrue(Member member);

    /**
     * 토큰 값으로 활성화된 리프레시 토큰 조회
     */
    Optional<RefreshToken> findByTokenValueAndIsActiveTrue(String tokenValue);

    /**
     * 특정 회원의 모든 리프레시 토큰 비활성화
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.member = :memberId")
    void deactivateAllByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 토큰 ID의 리프레시 토큰 비활성화
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.tokenId = :tokenId")
    void deactivateByTokenId(@Param("tokenId") String tokenId);

    /**
     * 만료된 토큰들 정리 (배치 작업용)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.isActive = false")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 특정 회원의 활성화된 토큰 개수 조회
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.member = :memberId AND rt.isActive = true")
    long countActiveTokensByMemberId(@Param("memberId") Long memberId);

}
