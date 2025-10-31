package fa25.group.evtrainticket.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.ScheduleStop;
import fa25.group.evtrainticket.repository.ScheduleStopRepository;
import fa25.group.evtrainticket.service.ScheduleStopService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleStopServiceImpl implements ScheduleStopService {

    private final ScheduleStopRepository scheduleStopRepository;

    @Override
    public List<ScheduleStop> getAllScheduleStops() {
        return scheduleStopRepository.findAll();
    }

    @Override
    public ScheduleStop getScheduleStopById(Integer id) {
        return scheduleStopRepository.findById(id).orElse(null);
    }

    @Override
    public ScheduleStop saveScheduleStop(ScheduleStop scheduleStop) {
        return scheduleStopRepository.save(scheduleStop);
    }

    @Override
    public void deleteScheduleStop(Integer id) {
        scheduleStopRepository.deleteById(id);
    }

    @Override
    public List<ScheduleStop> getScheduleStopsBySchedule(Schedule schedule) {
        return scheduleStopRepository.findBySchedule(schedule);
    }
}