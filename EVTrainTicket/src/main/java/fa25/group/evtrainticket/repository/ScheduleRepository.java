package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Station;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByDepartureStationAndArrivalStationAndDepartureTimeBetween(Station departureStation, Station arrivalStation, LocalDateTime startOfDay, LocalDateTime endOfDay);
    List<Schedule> findByDepartureStationStationIDAndArrivalStationStationIDAndDepartureTimeBetweenAndStatus(
            int departureId,
            int arrivalId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    );

    @Query("SELECT s FROM Schedule s WHERE s.departureStation.stationID = :stationId OR s.arrivalStation.stationID = :stationId")
    List<Schedule> findSchedulesByStation(@Param("stationId") Integer stationId);
}