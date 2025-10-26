package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // Đọc giá trị từ application.properties
    @Value("${app.otp.expiration-time:90000}")
    private long otpExpirationTime;

    @Override
    public void sendOTPEmail(String toEmail, String otpCode) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mã OTP khôi phục mật khẩu - EVTrainTicket");

            Context context = new Context();
            context.setVariable("otpCode", otpCode);

            // Tính toán số phút từ millisecond
            long totalSeconds = otpExpirationTime / 1000; // 90000 -> 90
            long expiryMinutes = totalSeconds / 60;       // 90 / 60 -> 1
            long expirySeconds = totalSeconds % 60;

            context.setVariable("expiryMinutes", expiryMinutes);
            context.setVariable("expirySeconds", expirySeconds);

            String htmlContent = templateEngine.process("email/otp-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new Exception("Lỗi khi gửi email OTP: " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetSuccessEmail(String toEmail, String fullName) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu thành công - EVTrainTicket");

            Context context = new Context();
            context.setVariable("fullName", fullName);

            String htmlContent = templateEngine.process("email/password-reset-success", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new Exception("Lỗi khi gửi email thông báo: " + e.getMessage());
        }
    }
}