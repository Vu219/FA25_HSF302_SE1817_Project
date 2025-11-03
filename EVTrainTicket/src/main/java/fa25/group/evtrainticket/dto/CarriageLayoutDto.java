package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarriageLayoutDto {
    private Integer carriageId;
    private Integer carriageNumber;
    private String carriageTypeName;
    private Integer totalSeats;
    private List<seatDto> seats;
}
