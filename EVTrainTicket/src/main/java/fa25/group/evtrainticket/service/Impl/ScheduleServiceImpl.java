package fa25.group.evtrainticket.service.Impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Seat;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.repository.ScheduleRepository;
import fa25.group.evtrainticket.repository.SeatRepository;
import fa25.group.evtrainticket.service.ScheduleService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
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
    public List<Seat> getSeatsByScheduleId(Integer scheduleId) {
        return seatRepository.findByCarriage_Train_Schedules_ScheduleID(scheduleId);
    }
}