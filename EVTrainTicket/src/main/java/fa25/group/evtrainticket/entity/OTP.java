package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "OTPs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OTPID", nullable = false)
    private Integer otpID;

    @Column(name = "Email", nullable = false)
    @NotNull(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Column(name = "OTPCode", nullable = false, length = 6)
    @Size(min = 6, max = 6, message = "Mã OTP phải có 6 chữ số")
    private String otpCode;

    @Column(name = "ExpiryTime", nullable = false)
    @NotNull(message = "Thời gian hết hạn không được để trống")
    private LocalDateTime expiryTime;

    @Column(name = "Used", nullable = false)
    private Boolean used = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}