package com.company.edu.service.user;

import com.company.edu.dto.auth.LoginResponseDto;
import com.company.edu.dto.auth.RefreshTokenRequestDto;
import com.company.edu.dto.auth.RefreshTokenResponseDto;
import com.company.edu.dto.user.LoginRequestDto;
import com.company.edu.dto.user.SearchIdRequestDto;
import com.company.edu.dto.user.SearchPwdRequestDto;
import com.company.edu.dto.user.SignUpRequestDto;

public interface AuthService {

    public LoginResponseDto login(LoginRequestDto dto);

    public Long signUp(SignUpRequestDto dto);

    public String searchId(SearchIdRequestDto dto);

    public String searchPwd(SearchPwdRequestDto dto);

    /**
     * 토큰 재발급 처리
     *
     * @param requestDto 토큰 재발급 요청 정보
     * @return 새로운 토큰 정보
     */
    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto requestDto);

    /**
     * 로그아웃 처리
     *
     * @param refreshToken 리프레시 토큰
     */
    void logout(String refreshToken);

    /**
     * 특정 회원의 모든 토큰 무효화
     *
     * @param memberId 회원 ID
     */
    void revokeAllTokens(Long memberId);
}
