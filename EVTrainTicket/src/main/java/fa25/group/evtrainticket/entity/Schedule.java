package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    @JoinColumn(name = "DepartureStationID", nullable = false)
    private Station departureStation;

    @ManyToOne
    @JoinColumn(name = "ArrivalStationID", nullable = false)
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
    private BigDecimal basePrice;

    @Column(name = "Status", nullable = false, columnDefinition = "nvarchar(50)")
    private String status;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "Notes", length = 500, columnDefinition = "nvarchar(500)")
    private String notes;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleStop> scheduleStops = new ArrayList<>();
}
