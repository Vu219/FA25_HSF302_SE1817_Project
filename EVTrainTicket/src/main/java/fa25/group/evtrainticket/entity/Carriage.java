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
@Table(name = "Carriages")
@Getter
@Setter
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
    @JsonIgnore
    private List<Seat> seats = new ArrayList<>();

    // @Override
    // public String toString() {
    //     return "Carriage{" +
    //             "carriageID=" + carriageID +
    //             ", carriageNumber='" + carriageNumber + '\'' +
    //             ", position=" + position +
    //             ", status='" + status + '\'' +
    //             '}';
    // }
}
