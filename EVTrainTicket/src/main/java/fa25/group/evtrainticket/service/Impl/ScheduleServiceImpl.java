package fa25.group.evtrainticket.service.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import fa25.group.evtrainticket.dto.RoundTripSearchDto;
import fa25.group.evtrainticket.dto.ScheduleSearchDto;
import org.springframework.stereotype.Service;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.repository.ScheduleRepository;
import fa25.group.evtrainticket.service.ScheduleService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private static final String STATUS_SCHEDULED = "Active";

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public Schedule getScheduleById(Integer id) {
        return scheduleRepository.findById(id).orElse(null);
    }

    @Override
    public Schedule saveSchedule(Schedule schedule) {
        // Basic validation, more complex validation will be added later
        if (schedule.getDepartureStation() == null || schedule.getArrivalStation() == null) {
            throw new IllegalArgumentException("Departure and Arrival stations cannot be null.");
        }
        if (schedule.getDepartureTime() == null || schedule.getArrivalTime() == null) {
            throw new IllegalArgumentException("Departure and Arrival times cannot be null.");
        }
        if (schedule.getDepartureTime().isAfter(schedule.getArrivalTime())) {
            throw new IllegalArgumentException("Departure time cannot be after arrival time.");
        }
        return scheduleRepository.save(schedule);
    }

    @Override
    public void deleteSchedule(Integer id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public List<Schedule> findSchedulesByStationsAndDate(Station departureStation, Station arrivalStation, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return scheduleRepository.findByDepartureStationAndArrivalStationAndDepartureTimeBetween(departureStation, arrivalStation, startOfDay, endOfDay);
    }

    @Override
    public List<ScheduleSearchDto> findAndMapSchedules(int depId, int arrId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Schedule> schedules = scheduleRepository.findByDepartureStationStationIDAndArrivalStationStationIDAndDepartureTimeBetweenAndStatus(
                depId,
                arrId,
                startOfDay,
                endOfDay,
                STATUS_SCHEDULED
        );
        return schedules.stream()
                .map(ScheduleSearchDto::new)
                .toList();
    }

    @Override
    public List<RoundTripSearchDto> searchSchedules(int depId, int arrId, LocalDate depDate, LocalDate returnDate, boolean isRoundTrip) {
        List<ScheduleSearchDto> departureSchedules = findAndMapSchedules(depId, arrId, depDate);
        List<ScheduleSearchDto> returnSchedules = null;
        if (isRoundTrip && returnDate !=null) {
            returnSchedules = findAndMapSchedules(arrId, depId, returnDate);
        }
        RoundTripSearchDto search = new RoundTripSearchDto(departureSchedules,returnSchedules);
        return List.of(search);
    }
}