package fa25.group.evtrainticket.Service;

import fa25.group.evtrainticket.Repository.ScheduleRepository;
import fa25.group.evtrainticket.dto.RoundTripSearchDto;
import fa25.group.evtrainticket.dto.ScheduleSearchDto;
import fa25.group.evtrainticket.entity.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements  ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    private static final String STATUS_SCHEDULED = "scheduled";

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
                .collect(Collectors.toList());
    }

    @Override
    public List<RoundTripSearchDto> searchSchedules(int depId, int arrId, LocalDate depDate, LocalDate returnDate, boolean isRoundTrip) {
        List<ScheduleSearchDto> departureSchedules = findAndMapSchedules(depId, arrId, depDate);
        List<ScheduleSearchDto> returnSchedules = null;
        if (isRoundTrip && returnDate !=null) {
            returnSchedules = findAndMapSchedules(arrId, depId, returnDate);
        }
        RoundTripSearchDto search = new RoundTripSearchDto(departureSchedules,returnSchedules);
        return Arrays.asList(search);
    }
}
