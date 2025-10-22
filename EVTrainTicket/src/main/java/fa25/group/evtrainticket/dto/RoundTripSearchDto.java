package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoundTripSearchDto{
    private List<ScheduleSearchDto> departureSchedules;
    private List<ScheduleSearchDto> returnSchedules;

}
