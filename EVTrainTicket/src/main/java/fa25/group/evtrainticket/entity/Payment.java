package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID", nullable = false)
    private Integer paymentID;

    @ManyToOne
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "PaymentMethod", nullable = false)
    private String paymentMethod;

    @Column(name = "PaymentDate", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "TransactionCode", nullable = false, length = 100)
    private String transactionCode;

    @Column(name = "Status", nullable = false, columnDefinition = "nvarchar(50)")
    private String status;

    @Column(name = "Notes", length = 500, columnDefinition = "nvarchar(500)")
    private String notes;

//    @CreationTimestamp
//    private Instant succeededAt;
}
