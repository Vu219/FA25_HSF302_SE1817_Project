package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.seatDto;
import java.util.List;

public interface SeatService {
    List<CarriageLayoutDto> getSeatLayout(int scheduleId);
    List<seatDto> getSeatsByScheduleId(Integer scheduleId);
    List<seatDto> getAvailableSeatsByScheduleId(Integer scheduleId);
}
