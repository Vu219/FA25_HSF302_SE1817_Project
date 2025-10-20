package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingID", nullable = false)
    private Integer bookingID;

    @Column(name = "BookingCode", nullable = false, length = 50)
    private String bookingCode;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "BookingDate", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "TotalAmount", nullable = false)
    private Double totalAmount;

    @Column(name = "Status", nullable = false, columnDefinition = "nvarchar(20)")
    private String status;

    @Column(name = "Notes", length = 500, columnDefinition = "nvarchar(500)")
    private String notes;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket>  tickets = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
}