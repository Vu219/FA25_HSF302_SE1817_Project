package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
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
    private String fullName;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "Phone", nullable = false, length = 13)
    private String phone;

    @Column(name = "Role", nullable = false)
    private String role;

    @Column(name = "CreateAt", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "Status", nullable = false, columnDefinition = "nvarchar(50)")
    private String status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();
}
