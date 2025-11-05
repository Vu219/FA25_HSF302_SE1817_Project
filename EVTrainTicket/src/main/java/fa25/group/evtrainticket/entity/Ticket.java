package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Tickets")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketID", nullable = false)
    private Integer ticketID;

    @ManyToOne
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "ScheduleID", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "SeatID", nullable = false)
    private Seat seat;

    @Column(name = "Price", nullable = false)
    private Double price;

    @Column(name = "TicketType", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String ticketType;

    @Column(name = "TicketCode", nullable = false, length = 50)
    private String ticketCode;

    @Column(name = "Status", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String status;

//    @Column(name = "CreatedAt", nullable = false)
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    protected void onCreate() {
//        if (createdAt == null) {
//            createdAt = LocalDateTime.now();
//        }
//    }
}
