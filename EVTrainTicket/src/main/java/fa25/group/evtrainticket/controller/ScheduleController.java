package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.ScheduleService;
import fa25.group.evtrainticket.service.SeatService;
import fa25.group.evtrainticket.service.RouteService;
import fa25.group.evtrainticket.service.StationService;
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
    @Autowired
    private RouteService routeService;
    @Autowired
    private StationService stationService;

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

    /**
     * API tính toán khoảng cách & thời gian giữa 2 ga
     * Support cả direct routes và indirect routes (tính tổng segments liền kề)
     * @param fromStationId ID ga đi
     * @param toStationId ID ga đến
     * @return Map chứa: distance_km, duration_min, isIndirect, routeId (nếu direct)
     */
    @GetMapping("/api/routes/distance")
    @ResponseBody
    public ResponseEntity<?> calculateDistance(@RequestParam("fromStationId") Integer fromStationId,
                                               @RequestParam("toStationId") Integer toStationId) {
        try {
            var fromStation = stationService.getStationsByID(fromStationId);
            var toStation = stationService.getStationsByID(toStationId);

            System.out.println("\n🔍 DEBUG: calculateDistance called");
            System.out.println("   From: " + (fromStation != null ? fromStation.getName() : "NULL"));
            System.out.println("   To: " + (toStation != null ? toStation.getName() : "NULL"));

            if (fromStation == null || toStation == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Không tìm thấy ga tàu"));
            }

            // DEBUG: Print all routes in DB
            var allRoutes = routeService.getAllRoutes();
            System.out.println("   Total routes in DB: " + allRoutes.size());
            for (var route : allRoutes) {
                System.out.println("     - " + route.getFromStation().getName() + " → " +
                                   route.getToStation().getName() + " (" + route.getDistanceKm() + " km)");
            }

            var result = routeService.calculateRouteDistance(fromStation, toStation);
            System.out.println("   Result: " + result);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("❌ ERROR in calculateDistance: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                Map.of("error", "Lỗi khi tính toán: " + e.getMessage()));
        }
    }
}