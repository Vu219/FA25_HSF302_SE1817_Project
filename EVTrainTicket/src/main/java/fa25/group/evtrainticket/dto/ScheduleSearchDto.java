package fa25.group.evtrainticket.dto;

import fa25.group.evtrainticket.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleSearchDto {
    private int scheduleId;
    private String trainName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal basePrice;

    public ScheduleSearchDto(Schedule schedule) {
        this.scheduleId = schedule.getScheduleID();
        this.trainName = schedule.getTrain().getTrainName();
        this.departureTime = schedule.getDepartureTime();
        this.arrivalTime = schedule.getArrivalTime();
        this.basePrice = schedule.getBasePrice();
    }
}
