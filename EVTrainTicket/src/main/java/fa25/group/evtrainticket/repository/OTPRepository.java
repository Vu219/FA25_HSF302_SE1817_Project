package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Integer> {

    // Tìm OTP hợp lệ chưa sử dụng
    public Optional<OTP> findByEmailAndOtpCodeAndUsedFalse(String email, String otpCode);

    // Tìm OTP mới nhất chưa sử dụng cho email
    @Query("SELECT o FROM OTP o WHERE o.email = :email AND o.used = false AND o.expiryTime > :now ORDER BY o.createdAt DESC")
    public Optional<OTP> findLatestValidOTP(@Param("email") String email, @Param("now") LocalDateTime now);

    // Đánh dấu tất cả OTP của email là đã sử dụng
    @Modifying
    @Query("UPDATE OTP o SET o.used = true WHERE o.email = :email AND o.used = false")
    public void invalidateAllOTPsForEmail(@Param("email") String email);

    // Xóa tất cả OTP đã hết hạn hoặc đã sử dụng
    @Modifying
    @Query("DELETE FROM OTP o WHERE o.expiryTime < :now OR o.used = true")
    public void deleteExpiredOrUsedOTPs(@Param("now") LocalDateTime now);

    // Đếm số OTP chưa sử dụng cho email
    public int countByEmailAndUsedFalse(String email);
}