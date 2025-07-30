package com.company.edu.controller.user;

import com.company.edu.config.user.CustomUserDetails;
import com.company.edu.dto.auth.*;
import com.company.edu.dto.user.*;
import com.company.edu.service.auth.PasswordResetService;
import com.company.edu.service.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final AuthService authService;

    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> getMemberProfile(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = this.authService.login(request);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.getAccessToken())  // 헤더에 토큰 추가
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        try {
            RefreshTokenResponseDto response = authService.refreshToken(request);

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + response.getAccessToken())
                    .body(response);
        } catch (Exception e) {
            log.error("토큰 재발급 실패", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(RefreshTokenResponseDto.builder()
                            .message("토큰 재발급 실패: " + e.getMessage())
                            .build());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody LogoutRequestDto request) {
        try {
            authService.logout(request.getRefreshToken());

            Map<String, String> response = new HashMap<>();
            response.put("message", "로그아웃 성공");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("로그아웃 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "로그아웃 처리 중 오류가 발생했습니다.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/revoke-all")
    public ResponseEntity<Map<String, String>> revokeAllTokens() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long memberId = userDetails.getMember().getMemberId();

            authService.revokeAllTokens(memberId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "모든 토큰이 무효화되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("토큰 무효화 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "토큰 무효화 처리 중 오류가 발생했습니다.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @PostMapping("/signup")
    public ResponseEntity<Long> signUp(@Valid @RequestBody SignUpRequestDto request) {

        Long memberId = this.authService.signUp(request);
        return ResponseEntity.status(HttpStatus.OK).body(memberId);
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            CustomUserInfoDto userInfo = userDetails.getMember();

            // 응답 데이터 구성 (비밀번호는 제외)
            Map<String, Object> response = new HashMap<>();
            response.put("memberId", userInfo.getMemberId());
            response.put("name", userInfo.getName());
            response.put("email", userInfo.getEmail());
            response.put("role", userInfo.getRole().name());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("사용자 정보 조회 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/search_id")
    public String searchId(@RequestBody SearchIdRequestDto request) {
        return authService.searchId(request);
    }

    @PostMapping("/search_pwd")
    public ResponseEntity<Void> forgotPassword(@RequestBody SearchPwdRequestDto request) {
        passwordResetService.initiatePasswordReset(request);
        // 항상 200 OK를 반환하여 이메일 존재 여부 추측 방지
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
