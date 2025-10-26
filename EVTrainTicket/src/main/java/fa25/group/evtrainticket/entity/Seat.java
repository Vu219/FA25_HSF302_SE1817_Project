package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Seats")
@Getter
@Setter
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

//    @Column(name = "Position", nullable = false)
//    private String position;

    @Column(name = "RowNum", nullable = false)
    private Integer rowNum;

    @Column(name = "ColumnNum", nullable = false)
    private Integer columnNum;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore 
    private List<Ticket> tickets = new ArrayList<>();
}