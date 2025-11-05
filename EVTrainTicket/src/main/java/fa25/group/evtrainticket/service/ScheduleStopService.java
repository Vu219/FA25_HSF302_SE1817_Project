package fa25.group.evtrainticket.service;

import java.util.List;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.ScheduleStop;

public interface ScheduleStopService {
    List<ScheduleStop> getAllScheduleStops();
    ScheduleStop getScheduleStopById(Integer id);
    ScheduleStop saveScheduleStop(ScheduleStop scheduleStop);
    void deleteScheduleStop(Integer id);

    List<ScheduleStop> getScheduleStopsBySchedule(Schedule schedule);
}