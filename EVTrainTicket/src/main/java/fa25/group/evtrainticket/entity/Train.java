package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Trains")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TrainID", nullable = false)
    private Integer trainID;

    @Column(name = "TrainNumber", nullable = false)
    private String trainNumber;

    @Column(name = "TrainName", nullable = false, length = 255, columnDefinition = "nvarchar(255)")
    private String trainName;

    @Column(name = "Capacity", nullable = false)
    private Integer capacity;

    @Column(name = "Status", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String status;

    @Column(name = "Notes", length = 500, columnDefinition = "nvarchar(500)")
    private String notes;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Carriage> carriages = new ArrayList<>();

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();
}
