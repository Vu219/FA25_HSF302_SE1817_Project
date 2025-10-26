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
@Table(name = "SeatTypes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SeatTypeID", nullable = false)
    private Integer seatTypeID;

    @Column(name = "TypeName", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String typeName;

    @Column(name = "PriceMultiplier", nullable = false)
    private Double priceMultiplier;

    @Column(name = "Description", length = 200, columnDefinition = "nvarchar(200)")
    private String description;

    @OneToMany(mappedBy = "seatType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Seat> seats = new ArrayList<>();
}
