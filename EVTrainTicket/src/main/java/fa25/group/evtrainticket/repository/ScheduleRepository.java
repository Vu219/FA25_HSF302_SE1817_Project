package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Station;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByDepartureStationAndArrivalStationAndDepartureTimeBetween(Station departureStation, Station arrivalStation, LocalDateTime startOfDay, LocalDateTime endOfDay);
}