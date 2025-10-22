package fa25.group.evtrainticket.Controller;

import fa25.group.evtrainticket.Service.ScheduleService;
import fa25.group.evtrainticket.Service.SeatService;
import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.RoundTripSearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/schedules")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private SeatService seatService;

    @GetMapping("/search")
    public ResponseEntity<RoundTripSearchDto> searchSchedules(
            @RequestParam int departureStationId,
            @RequestParam int arrivalStationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(defaultValue = "false") boolean isRoundTrip,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate
            ) {
        List<RoundTripSearchDto> resultsList = scheduleService.searchSchedules(departureStationId, arrivalStationId, departureDate, returnDate, isRoundTrip);
        RoundTripSearchDto results = resultsList.get(0);
        return ResponseEntity.ok(results);
    }
    @GetMapping("/{scheduleId}/seats")
    public ResponseEntity<List<CarriageLayoutDto>> getSeatLayoutForSchedule(
            @PathVariable int scheduleId) {

        List<CarriageLayoutDto> seatLayout = seatService.getSeatLayout(scheduleId);
        return ResponseEntity.ok(seatLayout);
    }
}
