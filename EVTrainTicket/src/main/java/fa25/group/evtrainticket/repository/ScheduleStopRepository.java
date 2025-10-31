package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.ScheduleStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleStopRepository extends JpaRepository<ScheduleStop, Integer> {
    List<ScheduleStop> findBySchedule(Schedule schedule);
}