package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.dto.RoundTripSearchDto;
import fa25.group.evtrainticket.dto.ScheduleSearchDto;
import fa25.group.evtrainticket.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Station;

public interface ScheduleService {
    List<Schedule> getAllSchedules();
    Schedule getScheduleById(Integer id);
    Schedule saveSchedule(Schedule schedule);
    void deleteSchedule(Integer id);

    List<Schedule> findSchedulesByStationsAndDate(Station departureStation, Station arrivalStation, LocalDateTime startOfDay, LocalDateTime endOfDay);
    List<ScheduleSearchDto> findAndMapSchedules(int depId, int arrId, LocalDate date);
    List<RoundTripSearchDto> searchSchedules(int depId, int arrId, LocalDate depDate, LocalDate returnDate, boolean isRoundTrip);
}