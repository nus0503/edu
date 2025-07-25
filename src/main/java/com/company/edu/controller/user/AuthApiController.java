package com.company.edu.controller.user;

import com.company.edu.config.user.CustomUserDetails;
import com.company.edu.dto.user.CustomUserInfoDto;
import com.company.edu.dto.user.LoginRequestDto;
import com.company.edu.dto.user.SignUpRequestDto;
import com.company.edu.service.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> getMemberProfile(@Valid @RequestBody LoginRequestDto request) {
        String token = this.authService.login(request);
        return ResponseEntity.ok()
                .header("Authorization", token)  // 헤더에 토큰 추가
                .body(Map.of(
                        "message", "로그인 성공",
                        "token", token  // 본문에도 토큰 포함 (백업용)
                ));
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
}
