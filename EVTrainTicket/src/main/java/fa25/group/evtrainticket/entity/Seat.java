package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "SeatNumber", nullable = false)
    private String seatNumber;

    @ManyToOne
    @JoinColumn(name = "SeatTypeID", nullable = false)
    private SeatType seatType;

    @Column(name = "RowNum", nullable = false)
    private Integer rowNum;

    @Column(name = "ColumnNum", nullable = false)
    private Integer columnNum;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();
}
