package fa25.group.evtrainticket.Service;

import fa25.group.evtrainticket.dto.CarriageLayoutDto;


import java.time.LocalDateTime;
import java.util.List;

public interface SeatService {
    List<CarriageLayoutDto> getSeatLayout(int scheduleId);

}
