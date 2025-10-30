package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.dto.RoundTripSearchDto;
import fa25.group.evtrainticket.dto.ScheduleSearchDto;
import fa25.group.evtrainticket.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<ScheduleSearchDto> findAndMapSchedules(int depId, int arrId, LocalDate date);
    List<RoundTripSearchDto> searchSchedules(int depId, int arrId, LocalDate depDate, LocalDate returnDate, boolean isRoundTrip);
    Schedule getScheduleById(int scheduleId);
}
