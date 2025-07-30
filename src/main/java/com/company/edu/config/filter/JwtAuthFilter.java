package com.company.edu.config.filter;

import com.company.edu.config.user.CustomUserDetailsService;
import com.company.edu.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/uploads/**",
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/refresh",           // 토큰 재발급
            "/api/v1/auth/search_id",         // 아이디 찾기
            "/api/v1/auth/search_pwd",        // 비밀번호 찾기
            "/api/v1/auth/reset",             // 비밀번호 재설정
            "/error",                         // 에러 페이지
            "/favicon.ico"                    // 파비콘
    );

    /**
     * JWT 토큰 검증 필터 수행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("🔍 JWT Filter 처리: {} {}", method, requestURI);

        String authorizationHeader = request.getHeader("Authorization");

        // JWT가 헤더에 있는 경우
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                // JWT 유효성 검증
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserId(token);
                    log.debug("✅ 유효한 토큰 - 사용자 ID: {}", userId);

                    // 유저와 토큰 일치 시 userDetails 생성
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId.toString());

                    if (userDetails != null) {
                        // UserDetails, Password, Role -> 접근권한 인증 Token 생성
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        // 현재 Request의 Security Context에 접근권한 설정
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        log.debug("🔐 SecurityContext 설정 완료 - 사용자: {}", userDetails.getUsername());
                    } else {
                        log.warn("⚠️ UserDetails 조회 실패 - 사용자 ID: {}", userId);
                        sendUnauthorizedResponse(response, "사용자 정보를 찾을 수 없습니다.");
                        return;
                    }
                } else {
                    // 토큰이 무효한 경우
                    log.warn("❌ 무효한 토큰 - URI: {}", requestURI);
                    sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
                    return;
                }
            } catch (Exception e) {
                // 토큰 처리 중 예외 발생
                log.error("💥 토큰 검증 중 오류 발생 - URI: {}", requestURI, e);
                sendUnauthorizedResponse(response, "토큰 검증 중 오류가 발생했습니다.");
                return;
            }
        } else {
            // Authorization 헤더가 없거나 형식이 잘못된 경우
            log.debug("🔒 Authorization 헤더 없음 - URI: {}", requestURI);
            sendUnauthorizedResponse(response, "인증 토큰이 필요합니다.");
            return;
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }


    /**
     * 401 Unauthorized 응답 전송
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", message);
        errorResponse.put("status", 401);
        errorResponse.put("timestamp", System.currentTimeMillis());

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        log.debug("📤 401 응답 전송: {}", message);
    }



    // filter 적용 제외 URL
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        return EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }
}
