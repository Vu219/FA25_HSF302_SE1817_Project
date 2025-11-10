package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarriageLayoutDto {
    private Integer carriageId;
    private String carriageNumber;
    private String carriageTypeName;
    private Integer totalSeats;
    private List<seatDto> seats;
    private Map<Integer, List<seatDto>> seatsByRow;
    private Integer maxColumns;

    // Constructor for backward compatibility with existing service
    public CarriageLayoutDto(Integer carriageId, String carriageNumber, String carriageTypeName, Integer totalSeats, List<seatDto> seats) {
        this.carriageId = carriageId;
        this.carriageNumber = carriageNumber;
        this.carriageTypeName = carriageTypeName;
        this.totalSeats = totalSeats;
        this.seats = seats;
    }
}
