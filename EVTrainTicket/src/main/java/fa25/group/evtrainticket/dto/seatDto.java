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
    private Integer seatID;
    private String seatNumber;
    private Boolean isAvailable;
    private Integer rowNumber;
    private String carriageNumber;
    private String seatTypeName;
    private BigDecimal price;
    private SeatStatus status;

    // Constructor for the service implementation
    public seatDto(Seat seat, BigDecimal finalPrice, SeatStatus seatStatus) {
        this.seatID = seat.getSeatID();
        this.seatNumber = seat.getSeatNumber();
        this.isAvailable = seat.getIsAvailable();
        this.rowNumber = seat.getRowNumber();
        this.carriageNumber = seat.getCarriage().getCarriageNumber();
        this.seatTypeName = seat.getSeatType().getTypeName();
        this.price = finalPrice;
        this.status = seatStatus;
    }
}
