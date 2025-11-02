package fa25.group.evtrainticket.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.service.ScheduleService;
import fa25.group.evtrainticket.service.ScheduleStopService;
import fa25.group.evtrainticket.service.StationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class UserSearchController {

    private final ScheduleService scheduleService;
    private final StationService stationService;
    private final ScheduleStopService scheduleStopService;

    @GetMapping
    public String showSearchForm(Model model, HttpSession session) {
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("user", session.getAttribute("user"));
        return "user/search/form";
    }

    @GetMapping("/results")
    public String searchSchedules(
            @RequestParam("departureStationId") Integer departureStationId,
            @RequestParam("arrivalStationId") Integer arrivalStationId,
            @RequestParam("departureDate") String departureDate,
            Model model,
            HttpSession session) {
        try {
            // Always add stations and user for the form
            model.addAttribute("stations", stationService.getAllStations());
            model.addAttribute("user", session.getAttribute("user"));

            // Validate stations
            Station departureStation = stationService.getStationById(departureStationId);
            Station arrivalStation = stationService.getStationById(arrivalStationId);

            if (departureStation == null || arrivalStation == null) {
                model.addAttribute("error", "❌ Ga đi hoặc ga đến không tồn tại. Vui lòng chọn lại.");
                return "home";
            }

            // Check if same station
            if (departureStationId.equals(arrivalStationId)) {
                model.addAttribute("error", "❌ Ga đi và ga đến không được trùng nhau!");
                return "home";
            }

            // Parse date
            LocalDate date = LocalDate.parse(departureDate);
            LocalDate today = LocalDate.now();

            if (date.isBefore(today)) {
                model.addAttribute("error", "❌ Ngày đi không thể là ngày trong quá khứ!");
                return "home";
            }

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            // Search schedules
            List<Schedule> schedules = scheduleService.findSchedulesByStationsAndDate(
                    departureStation,
                    arrivalStation,
                    startOfDay,
                    endOfDay
            );

            // Add data to model
            model.addAttribute("schedules", schedules);
            model.addAttribute("departureStation", departureStation);
            model.addAttribute("arrivalStation", arrivalStation);
            model.addAttribute("departureDate", departureDate);

            // Check if no results
            if (schedules.isEmpty()) {
                model.addAttribute("message",
                        "ℹ️ Không tìm thấy chuyến tàu nào từ " + departureStation.getName() +
                                " đến " + arrivalStation.getName() +
                                " vào ngày " + departureDate + ". Vui lòng thử ngày khác.");
            }

            return "home";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "⚠️ Lỗi khi tìm kiếm: " + e.getMessage());
            model.addAttribute("stations", stationService.getAllStations());
            model.addAttribute("user", session.getAttribute("user"));
            return "home";
        }
    }

    @GetMapping("/details/{id}")
    public String viewTripDetails(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Schedule schedule = scheduleService.getScheduleById(id);
        if (schedule == null) {
            return "redirect:/home?error=scheduleNotFound";
        }
        model.addAttribute("schedule", schedule);
        model.addAttribute("scheduleStops", scheduleStopService.getScheduleStopsBySchedule(schedule));
        model.addAttribute("user", session.getAttribute("user"));
        return "user/search/details";
    }
}