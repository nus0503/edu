package com.company.edu.entity.auth;

import com.company.edu.entity.user.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token_id", nullable = false, unique = true)
    private String tokenId;

    @Column(name = "token_value", nullable = false, columnDefinition = "TEXT")
    private String tokenValue;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RefreshToken(Member member, String tokenId, String tokenValue,
                        LocalDateTime expiresAt) {
        this.member = member;
        this.tokenId = tokenId;
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return this.isActive && !isExpired();
    }
}
