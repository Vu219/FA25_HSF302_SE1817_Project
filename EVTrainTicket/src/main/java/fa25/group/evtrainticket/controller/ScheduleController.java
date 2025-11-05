package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.ScheduleService;
import fa25.group.evtrainticket.service.SeatService;
import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private SeatService seatService;
    /**
     * API này được booking.html sử dụng để tải sơ đồ ghế
     */
    @GetMapping("/api/schedules/{scheduleId}/seats")
    @ResponseBody
    public ResponseEntity<List<CarriageLayoutDto>> getSeatLayoutForSchedule(
            @PathVariable("scheduleId") int scheduleId) {

        List<CarriageLayoutDto> seatLayout = seatService.getSeatLayout(scheduleId);
        return ResponseEntity.ok(seatLayout);
    }

    /**
     * API này được booking.html sử dụng để tải thông tin chi tiết chuyến tàu
     */
    @GetMapping("/api/schedules/{scheduleId}")
    @ResponseBody
    public ResponseEntity<?> getScheduleById(@PathVariable("scheduleId") int scheduleId) {
        try {
            var schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                return ResponseEntity.notFound().build();
            }

            // Create a simple response with basic schedule info
            var response = Map.of(
                    "scheduleId", schedule.getScheduleID(),
                    "trainName", schedule.getTrain().getTrainName(),
                    "departureStation", schedule.getDepartureStation().getName(),
                    "arrivalStation", schedule.getArrivalStation().getName(),
                    "departureTime", schedule.getDepartureTime().toString(),
                    "arrivalTime", schedule.getArrivalTime().toString(),
                    "basePrice", schedule.getBasePrice().doubleValue(),
                    "status", schedule.getStatus()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Schedule not found: " + e.getMessage());
        }
    }
}