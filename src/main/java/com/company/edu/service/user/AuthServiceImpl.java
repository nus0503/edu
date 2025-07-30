package com.company.edu.service.user;

import com.company.edu.common.code.error.UserErrorCode;
import com.company.edu.common.customException.RestApiException;
import com.company.edu.config.user.CustomUserDetails;
import com.company.edu.dto.auth.LoginResponseDto;
import com.company.edu.dto.auth.RefreshTokenRequestDto;
import com.company.edu.dto.auth.RefreshTokenResponseDto;
import com.company.edu.dto.user.*;
import com.company.edu.entity.auth.RefreshToken;
import com.company.edu.entity.user.Member;
import com.company.edu.entity.user.RoleType;
import com.company.edu.repository.auth.RefreshTokenRepository;
import com.company.edu.repository.user.MemberRepository;
import com.company.edu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.max-tokens-per-user}")
    private int maxTokensPerUser;

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();

        Member member = memberRepository.findMemberByEmail(email);

        if (member == null) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_USER);
        }

        if (!encoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        cleanupExpiredTokens(member.getMemberId());
        limitTokensPerUser(member.getMemberId());

        CustomUserInfoDto info = modelMapper.map(member, CustomUserInfoDto.class);
        String accessToken = jwtUtil.createAccessToken(info);
        JwtUtil.TokenInfo refreshTokenInfo = jwtUtil.createRefreshToken(member.getMemberId());

        RefreshToken refreshToken = RefreshToken.builder()
                .member(member)
                .tokenId(refreshTokenInfo.getTokenId())
                .tokenValue(refreshTokenInfo.getTokenValue())
                .expiresAt(refreshTokenInfo.getExpiresAt())
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info("로그인 성공 - 회원 ID: {}", member.getMemberId());
        return LoginResponseDto.of(accessToken, refreshTokenInfo.getTokenValue(), jwtUtil.getAccessTokenExpTime());
    }

    @Override
    @Transactional
    public Long signUp(SignUpRequestDto dto) {

        memberRepository.findByEmail(dto.getEmail())
                .ifPresent(member -> {
                    throw new RestApiException(UserErrorCode.NOT_SIGNUP_USER);
                });

        String password = dto.getPassword();
        String encodedPassword = encoder.encode(password);
        dto.setPassword(encodedPassword);
        Member member = modelMapper.map(dto, Member.class);
        member.setRole(RoleType.USER);
        return memberRepository.save(member).getMemberId();
    }


    @Override
    public String searchId(SearchIdRequestDto dto) {
        Member member = memberRepository.findByNameAndPhoneNumber(dto.getName(), dto.getPhoneNumber())
                .orElseThrow(() -> new RestApiException(UserErrorCode.NOT_FOUND_USER));

        return member.getEmail();
    }

    @Override
    public String searchPwd(SearchPwdRequestDto dto) {

        return "";
    }

    @Override
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto requestDto) {
        String refreshTokenValue = requestDto.getRefreshToken();

        if (!jwtUtil.validateRefreshToken(refreshTokenValue)) {
            throw new RestApiException(UserErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenValueAndIsActiveTrue(refreshTokenValue).orElseThrow(
                () -> new RestApiException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!refreshToken.isValid()) {
            refreshToken.deactivate();
            refreshTokenRepository.save(refreshToken);
            throw new RestApiException(UserErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Member member = memberRepository.findById(refreshToken.getMember().getMemberId()).orElseThrow(
                () -> new RestApiException(UserErrorCode.NOT_ACCESS_USER));

        CustomUserInfoDto userInfoDto = modelMapper.map(member, CustomUserInfoDto.class);
        String newAccessToken = jwtUtil.createAccessToken(userInfoDto);
        JwtUtil.TokenInfo newRefreshTokenInfo = jwtUtil.createRefreshToken(member.getMemberId());

        refreshToken.deactivate();
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .member(member)
                .tokenId(newRefreshTokenInfo.getTokenId())
                .tokenValue(newRefreshTokenInfo.getTokenValue())
                .expiresAt(newRefreshTokenInfo.getExpiresAt())
                .build();

        refreshTokenRepository.save(newRefreshToken);

        log.info("토큰 재발급 성공 - 회원 ID: {}", member.getMemberId());

        return RefreshTokenResponseDto.of(newAccessToken, newRefreshTokenInfo.getTokenValue(), jwtUtil.getAccessTokenExpTime());
    }

    @Override
    public void logout(String refreshTokenValue) {
        // 특정 토큰만 비활성화
        String tokenId = jwtUtil.getTokenId(refreshTokenValue);
        refreshTokenRepository.deactivateByTokenId(tokenId);
        log.info("로그아웃 - 토큰 ID: {}", tokenId);
    }

    @Override
    public void revokeAllTokens(Long memberId) {
        refreshTokenRepository.deactivateAllByMemberId(memberId);
        log.info("모든 토큰 무효화 - 회원 ID: {}", memberId);
    }


    /**
     * 만료된 토큰 정리
     */
    private void cleanupExpiredTokens(Long memberId) {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * 사용자당 토큰 개수 제한
     */
    private void limitTokensPerUser(Long memberId) {
        long activeTokenCount = refreshTokenRepository.countActiveTokensByMemberId(memberId);

        if (activeTokenCount >= maxTokensPerUser) {
            // 가장 오래된 토큰들을 비활성화
            refreshTokenRepository.deactivateAllByMemberId(memberId);
            log.info("토큰 개수 제한으로 기존 토큰들 정리 - 회원 ID: {}", memberId);
        }
    }

}
