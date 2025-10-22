package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarriageLayoutDto {
    private int carriageId;
    private String carriageNumber;
    private String carriageTypeName;
    private List<seatDto> seats;
}
