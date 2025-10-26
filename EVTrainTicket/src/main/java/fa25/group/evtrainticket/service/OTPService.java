package fa25.group.evtrainticket.service;

public interface OTPService {
    public String generateOTP();
    public void sendOTPToEmail(String email) throws Exception;
    public boolean verifyOTP(String email, String otpCode) throws Exception;
    public void cleanupExpiredOTPs();
}