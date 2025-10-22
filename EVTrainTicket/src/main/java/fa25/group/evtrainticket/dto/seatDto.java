package fa25.group.evtrainticket.dto;


import fa25.group.evtrainticket.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class seatDto {
    private Integer seatId;
    private String seatNumber;
    private String seatTypeName;
    private BigDecimal seatPrice;
    private SeatStatus seatStatus;
    private int rowNum;
    private int columnNum;

    public seatDto(Seat seat, BigDecimal finalPrice, SeatStatus seatStatus) {
    }
}
