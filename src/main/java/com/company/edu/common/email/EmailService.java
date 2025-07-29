package com.company.edu.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async // 이메일 발송은 비동기로 처리
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("비밀번호 재설정 요청");

        String resetUrl = frontendUrl + "/reset-password.html?token=" + token;
        message.setText("비밀번호를 재설정하려면 아래 링크를 클릭하세요. 이 링크는 10분간 유효합니다.\n" + resetUrl);

        mailSender.send(message);
    }

    @Async
    public void sendPasswordChangeNotification(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("비밀번호 변경 완료 안내");
        message.setText("회원님의 비밀번호가 성공적으로 변경되었습니다. 본인이 변경한 것이 아니라면 즉시 고객센터로 문의해주세요.");
        mailSender.send(message);
    }
}
