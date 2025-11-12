package fa25.group.evtrainticket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fa25.group.evtrainticket.dto.SeatStatus;
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

    @ManyToOne
    @JoinColumn(name = "SeatTypeID", nullable = false)
    private SeatType seatType;

    @Column(name = "SeatNumber", nullable = false, length = 10)
    private String seatNumber;

    @Column(name = "IsAvailable", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "RowNum", nullable = false)
    private Integer rowNumber;

    @Column(name = "ColumnNum", nullable = false)
    private Integer columnNum;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Ticket> tickets = new ArrayList<>();

    @Transient // Không lưu vào DB
    private SeatStatus status = SeatStatus.AVAILABLE;
}
