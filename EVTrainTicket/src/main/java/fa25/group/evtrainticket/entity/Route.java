package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"from_station_id", "to_station_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_station_id", nullable = false)
    private Station fromStation;

    @ManyToOne
    @JoinColumn(name = "to_station_id", nullable = false)
    private Station toStation;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "status")
    private String status; // ACTIVE, INACTIVE

    public Route(Station fromStation, Station toStation, Double distanceKm) {
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.distanceKm = distanceKm;
        this.status = "ACTIVE";
    }

    /**
     * Tính thời gian chạy (phút) dựa vào vận tốc tàu
     * duration_min = (distance_km / trainSpeed) * 60
     */
    public Integer calculateDuration(Double trainSpeedKmH) {
        if (trainSpeedKmH == null || trainSpeedKmH <= 0) {
            trainSpeedKmH = 60.0;  // default 60 km/h
        }
        long durationMinutes = Math.round((distanceKm / trainSpeedKmH) * 60);
        return (int) durationMinutes;
    }
}

