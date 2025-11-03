package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Seats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SeatID", nullable = false)
    private Integer seatID;

    @ManyToOne
    @JoinColumn(name = "CarriageID", nullable = false)
    private Carriage carriage;

    @ManyToOne
    @JoinColumn(name = "SeatTypeID", nullable = false)
    private SeatType seatType;

    @Column(name = "SeatNumber", nullable = false, length = 10)
    private String seatNumber;

    @Column(name = "IsAvailable", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "RowNum", nullable = false)
    private Integer rowNumber;

    @Column(name = "ColumnNum", nullable = false, length = 5)
    private String columnPosition; // A, B, C, D, etc.
}
