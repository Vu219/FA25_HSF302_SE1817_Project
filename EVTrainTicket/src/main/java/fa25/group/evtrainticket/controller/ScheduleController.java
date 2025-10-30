package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.ScheduleService;
import fa25.group.evtrainticket.service.SeatService;
import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.RoundTripSearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private SeatService seatService;

    @GetMapping("/")
    public String home() {
        return "Schedule";
    }

    @GetMapping("/schedule")
    public String schedule() {
        return "Schedule";
    }

    @GetMapping("/api/schedules/search")
    @ResponseBody
    public ResponseEntity<RoundTripSearchDto> searchSchedules(
            @RequestParam int departureStationId,
            @RequestParam int arrivalStationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(defaultValue = "false") boolean isRoundTrip,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate
            ) {
        List<RoundTripSearchDto> resultsList = scheduleService.searchSchedules(departureStationId, arrivalStationId, departureDate, returnDate, isRoundTrip);

        if (resultsList.isEmpty()) {
            // Return empty RoundTripSearchDto if no results found
            return ResponseEntity.ok(new RoundTripSearchDto());
        }

        RoundTripSearchDto results = resultsList.get(0);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/api/schedules/{scheduleId}/seats")
    @ResponseBody
    public ResponseEntity<List<CarriageLayoutDto>> getSeatLayoutForSchedule(
            @PathVariable int scheduleId) {

        List<CarriageLayoutDto> seatLayout = seatService.getSeatLayout(scheduleId);
        return ResponseEntity.ok(seatLayout);
    }

    @GetMapping("/api/schedules/{scheduleId}")
    @ResponseBody
    public ResponseEntity<?> getScheduleById(@PathVariable int scheduleId) {
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
