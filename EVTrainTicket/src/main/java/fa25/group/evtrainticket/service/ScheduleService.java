package fa25.group.evtrainticket.Service;

import fa25.group.evtrainticket.dto.RoundTripSearchDto;
import fa25.group.evtrainticket.dto.ScheduleSearchDto;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<ScheduleSearchDto> findAndMapSchedules(int depId, int arrId, LocalDate date);
    List<RoundTripSearchDto> searchSchedules(int depId, int arrId, LocalDate depDate, LocalDate returnDate, boolean isRoundTrip);
}
