package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.OTP;
import fa25.group.evtrainticket.repository.OTPRepository;
import fa25.group.evtrainticket.service.EmailService;
import fa25.group.evtrainticket.service.OTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {
    private final OTPRepository otpRepository;
    private final EmailService emailService;

    @Value("${app.otp.expiration-time:90000}")
    private long otpExpirationTime;

    private static final String NUMBERS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private final Random random = new Random();

    @Override
    public String generateOTP() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        return otp.toString();
    }

    @Override
    @Transactional
    public void sendOTPToEmail(String email) throws Exception {
        if (otpRepository.countByEmailAndUsedFalse(email) >= 3) {
            throw new Exception("Bạn đã yêu cầu quá nhiều OTP. Vui lòng thử lại sau.");
        }

        // Vô hiệu hóa OTP cũ
        otpRepository.invalidateAllOTPsForEmail(email);

        // Tạo OTP mới
        String otpCode = generateOTP();
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(otpExpirationTime / 1000);

        OTP otp = new OTP();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiryTime(expiryTime);
        otp.setUsed(false);

        otpRepository.save(otp);
        emailService.sendOTPEmail(email, otpCode);
    }

    @Override
    @Transactional
    public boolean verifyOTP(String email, String otpCode) throws Exception {
        OTP otp = otpRepository.findByEmailAndOtpCodeAndUsedFalse(email, otpCode)
                .orElseThrow(() -> new Exception("Mã OTP không tồn tại hoặc đã được sử dụng"));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new Exception("Mã OTP đã hết hạn");
        }

        // Đánh dấu đã sử dụng
        otp.setUsed(true);
        otpRepository.save(otp);
        return true;
    }

    @Override
    @Transactional
    public void cleanupExpiredOTPs() {
        otpRepository.deleteExpiredOrUsedOTPs(LocalDateTime.now());
    }

    // Tự động dọn dẹp OTP hết hạn mỗi phút
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scheduledCleanup() {
        cleanupExpiredOTPs();
    }
}