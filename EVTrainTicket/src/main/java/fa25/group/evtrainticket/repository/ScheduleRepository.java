package fa25.group.evtrainticket.Repository;


import fa25.group.evtrainticket.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByDepartureStationStationIDAndArrivalStationStationIDAndDepartureTimeBetweenAndStatus(
            int departureId,
            int arrivalId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    );
}
