package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Carriages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Carriage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CarriageID", nullable = false)
    private Integer carriageID;

    @ManyToOne
    @JoinColumn(name = "TrainID", nullable = false)
    private Train train;

    @Column(name = "CarriageNumber", nullable = false, length = 10)
    private String carriageNumber;

    @ManyToOne
    @JoinColumn(name = "CarriageTypeID", nullable = false)
    private CarriageType carriageType;

    @Column(name = "Position", nullable = false)
    private Integer  position;

    @Column(name = "Status", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String status;

    @OneToMany(mappedBy = "carriage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();
}
