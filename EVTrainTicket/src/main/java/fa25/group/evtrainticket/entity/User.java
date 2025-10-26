package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID", nullable = false)
    private Integer userID;

    @Column(name = "FullName", nullable = false, length = 100, columnDefinition = "nvarchar(100)")
    @NotNull(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @Column(name = "Email", nullable = false)
    @NotNull(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Column(name = "Password", nullable = false)
    @NotNull(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @Column(name = "Phone", nullable = false, length = 13)
    @Size(min = 10, max = 13, message = "Số điện thoại phải từ 10 đến 13 ký tự")
    private String phone;

    @Column(name = "Role", nullable = false)
    private String role;

    @Column(name = "CreateAt", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (phone == null) {
            phone = "";
        }
        if (role == null) {
            role = "USER";
        }
    }
}