package com.company.edu.service.auth;

import com.company.edu.common.code.error.UserErrorCode;
import com.company.edu.common.customException.RestApiException;
import com.company.edu.common.email.EmailService;
import com.company.edu.common.util.TokenGenerator;
import com.company.edu.dto.user.SearchPwdRequestDto;
import com.company.edu.entity.auth.PasswordResetToken;
import com.company.edu.entity.user.Member;
import com.company.edu.repository.auth.PasswordResetTokenRepository;
import com.company.edu.repository.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final RateLimitingService rateLimitingService;

    @Transactional
    public void initiatePasswordReset(SearchPwdRequestDto request) {

        if (!rateLimitingService.isAllowed(request.getName())) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY);
        }

        memberRepository.findByNameAndPhoneNumberAndEmail(request.getName(), request.getPhoneNumber(), request.getEmail()).ifPresent(member -> {

            tokenRepository.findByMember(member).ifPresent(tokenRepository::delete);

            String token = tokenGenerator.generateToken();
            PasswordResetToken resetToken = new PasswordResetToken(token, member);
            tokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(member.getEmail(), token);
        });
    }

    @Transactional
    // 2. 토큰을 이용한 비밀번호 재설정
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY);
        }

        Member user = resetToken.getMember();
        user.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(user);

        // 사용 완료된 토큰은 즉시 삭제 (1회용)
        tokenRepository.delete(resetToken);

        // 비밀번호 변경 완료 알림 메일 발송
        emailService.sendPasswordChangeNotification(user.getEmail());
    }

}
