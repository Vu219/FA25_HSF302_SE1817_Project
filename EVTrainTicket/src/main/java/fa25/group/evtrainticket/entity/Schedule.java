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

    @ManyToOne
    @JoinColumn(name = "RouteID")
    private Route route;

    @Column(name = "DepartureTime", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "ArrivalTime", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "BasePrice", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "Status", nullable = false, columnDefinition = "nvarchar(50)")
    private String status = "ACTIVE";

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "Notes", length = 500, columnDefinition = "nvarchar(500)")
    private String notes;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleStop> stops = new ArrayList<>();

    // ===== Getter methods =====
    public String getOrigin() {
        return departureStation != null ? departureStation.getName() : "";
    }

    public String getDestination() {
        return arrivalStation != null ? arrivalStation.getName() : "";
    }

    public Double getDistanceKm() {
        return route != null ? route.getDistanceKm() : 0.0;
    }

    public Integer getEstimatedTime() {
        if (route == null || train == null || train.getAverageSpeed() == null) {
            return 0;
        }
        return route.calculateDuration(train.getAverageSpeed());
    }

    /**
     * Tính giờ đến tự động từ:
     * - giờ đi (departureTime)
     * - khoảng cách (distanceKm từ route)
     * - vận tốc tàu trung bình (averageSpeed từ train)
     * @return LocalDateTime dự kiến đến
     */
    public LocalDateTime calculateArrivalTime() {
        if (departureTime == null || route == null || train == null || train.getAverageSpeed() == null) {
            return null;
        }

        // Tính thời gian chạy (phút) từ route
        int travelMinutes = route.calculateDuration(train.getAverageSpeed());

        return departureTime.plusMinutes(travelMinutes);
    }
}
