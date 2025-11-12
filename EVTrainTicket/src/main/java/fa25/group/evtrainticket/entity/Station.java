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
@Table(name = "Stations")
@Getter
@Setter
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

    @Column(name = "Province", nullable = true, length = 50, columnDefinition = "nvarchar(50)")
    private String province;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ScheduleStop> scheduleStops = new ArrayList<>();

    @OneToMany(mappedBy = "departureStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> departureSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "arrivalStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> arrivalSchedules = new ArrayList<>();
}
