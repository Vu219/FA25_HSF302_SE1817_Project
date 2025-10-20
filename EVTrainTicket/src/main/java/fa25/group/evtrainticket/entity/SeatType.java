package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SeatTypes")
@Data
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
    private List<Seat> seats = new ArrayList<>();
}
