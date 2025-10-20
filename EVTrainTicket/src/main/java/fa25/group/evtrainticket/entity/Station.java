package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Stations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StationID", nullable = false)
    private Integer stationID;

    @Column(name = "Name", nullable = false, length = 500, columnDefinition = "nvarchar(500)")
    private String name;

    @Column(name = "Code", nullable = false)
    private String code;

    @Column(name = "Address", nullable = false, length = 255, columnDefinition = "nvarchar(255)")
    private String address;

    @Column(name = "City", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String city;

    @Column(name = "Province", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String province;

    @Column(name = "Status", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String status;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleStop> scheduleStops = new ArrayList<>();

    @OneToMany(mappedBy = "departureStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> departureSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "arrivalStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> arrivalSchedules = new ArrayList<>();
}
