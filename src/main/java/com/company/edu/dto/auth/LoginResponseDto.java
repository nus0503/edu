package com.company.edu.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private String message;

    public static LoginResponseDto of(String accessToken, String refreshToken, Long expiresIn) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .message("로그인 성공")
                .build();
    }
}
