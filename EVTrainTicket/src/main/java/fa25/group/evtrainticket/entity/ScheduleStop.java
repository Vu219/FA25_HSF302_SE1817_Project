package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ScheduleStops")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StopId", nullable = false)
    private Integer stopId;

    @ManyToOne
    @JoinColumn(name = "ScheduleID", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "StationID", nullable = false)
    private Station station;

    @Column(name = "ArrivalTime", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "DepartureTime", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "StopOrder", nullable = false)
    private Integer stopOrder;

    @Column(name = "DistanceFromStart", nullable = false)
    private Double distanceFromStart;
}
