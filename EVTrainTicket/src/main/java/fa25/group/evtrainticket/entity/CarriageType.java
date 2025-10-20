package fa25.group.evtrainticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CarriageTypes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarriageType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CarriageTypeID", nullable = false)
    private Integer carriageTypeId;

    @Column(name = "TypeName", nullable = false, length = 50, columnDefinition = "nvarchar(50)")
    private String typeName;

    @Column(name = "SeatCount", nullable = false)
    private Integer seatCount;

    @Column(name = "Description", length = 255, columnDefinition = "nvarchar(255)")
    private String description;

    @Column(name = "PriceMultiplier", nullable = false)
    private Double priceMultiplier;

    @OneToMany(mappedBy = "carriageType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Carriage> carriages = new ArrayList<>();
}
