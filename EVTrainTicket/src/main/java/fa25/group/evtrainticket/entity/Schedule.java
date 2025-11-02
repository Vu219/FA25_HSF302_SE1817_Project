package fa25.group.evtrainticket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Schedules")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduleID", nullable = false)
    private Integer scheduleID;

    @ManyToOne
    @JoinColumn(name = "departureStationID", nullable = false)
    private Station departureStation;

    @ManyToOne
    @JoinColumn(name = "arrivalStationID", nullable = false)
    private Station arrivalStation;

    @ManyToOne
    @JoinColumn(name = "TrainID", nullable = false)
    private Train train;

    @Column(name = "DepartureTime", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "ArrivalTime", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "DistanceKm", nullable = false)
    private Double distanceKm;

    @Column(name = "EstimatedTime", nullable = false)
    private Integer estimatedTime;

    @Column(name = "BasePrice", nullable = false)
    private Double basePrice;

    @Column(name = "Status", nullable = false, columnDefinition = "nvarchar(50)")
    private String status = "ACTIVE";


    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "Notes", length = 500, columnDefinition = "nvarchar(500)")
    private String notes;

    @JsonIgnore
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleStop> scheduleStops = new ArrayList<>();
}
