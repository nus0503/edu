package com.company.edu.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenResponseDto {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private String message;

    public static RefreshTokenResponseDto of(String accessToken, String refreshToken, Long expiresIn) {
        return RefreshTokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .message("토큰 재발급 성공")
                .build();
    }
}
