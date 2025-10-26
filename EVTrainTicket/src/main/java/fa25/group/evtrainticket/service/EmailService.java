package fa25.group.evtrainticket.service;

public interface EmailService {
    void sendOTPEmail(String toEmail, String otpCode) throws Exception;
    void sendPasswordResetSuccessEmail(String toEmail, String fullName) throws Exception;
}