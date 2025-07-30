package com.company.edu.util;

import com.company.edu.common.util.TokenGenerator;
import com.company.edu.dto.user.CustomUserInfoDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@Getter
public class JwtUtil {

    private final Key key;
    private final long accessTokenExpTime;
    private final long refreshTokenExpTime;

    public JwtUtil(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration_time}") long accessTokenExpTime, @Value("${jwt.refresh_expiration_time}") long refreshTokenExpTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpTime = accessTokenExpTime;
        this.refreshTokenExpTime = refreshTokenExpTime;
    }

    /**
     * Access Token 생성
     *
     * @param member
     * @return Access Token String
     */
    public String createAccessToken(CustomUserInfoDto member) {
        return createToken(member, this.accessTokenExpTime, "access");
    }


    /**
     * Refresh Token 생성
     *
     * @param memberId 회원 ID
     * @return TokenInfo (토큰 값과 토큰 ID 포함)
     */
    public TokenInfo createRefreshToken(Long memberId) {
        String tokenId = UUID.randomUUID().toString();

        Claims claims = Jwts.claims();
        claims.put("memberId", memberId);
        claims.put("tokenId", tokenId);
        claims.put("type", "refresh");

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(refreshTokenExpTime);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();

        return TokenInfo.builder()
                .tokenValue(token)
                .tokenId(tokenId)
                .expiresAt(tokenValidity.toLocalDateTime())
                .build();
    }





    /**
     * JWT 생성
     * @param member
     * @param expireTime
     * @return JWT String
     */
    private String createToken(CustomUserInfoDto member, long expireTime, String tokenType) {
        Claims claims = Jwts.claims();
        claims.put("memberId", member.getMemberId());
        claims.put("email", member.getEmail());
        claims.put("role", member.getRole());
        claims.put("type", tokenType);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(expireTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Token에서 User ID 추출
     * @param token
     * @return User ID
     */
    public Long getUserId(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }


    /**
     * Refresh Token에서 Token ID 추출
     *
     * @param refreshToken Refresh Token
     * @return Token ID
     */
    public String getTokenId(String refreshToken) {
        return parseClaims(refreshToken).get("tokenId", String.class);
    }


    /**
     * 토큰 타입 확인
     *
     * @param token JWT 토큰
     * @return 토큰 타입 (access/refresh)
     */
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }


    /**
     * JWT 검증
     * @param token
     * @return IsValidate
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return "access".equals(claims.get("type", String.class));
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }


    /**
     * Refresh Token 검증
     *
     * @param refreshToken Refresh Token
     * @return 유효성 여부
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken).getBody();
            return "refresh".equals(claims.get("type", String.class));
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid Refresh Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired Refresh Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported Refresh Token", e);
        } catch (IllegalArgumentException e) {
            log.info("Refresh token claims string is empty.", e);
        }
        return false;
    }



    /**
     * JWT Claims 추출
     * @param accessToken
     * @return JWT Claims
     */
    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }






    /**
     * 토큰 정보를 담는 내부 클래스
     */
    @Builder
    @Getter
    public static class TokenInfo {
        private final String tokenValue;
        private final String tokenId;
        private final LocalDateTime expiresAt;
    }
}

