package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.seatDto;
import fa25.group.evtrainticket.service.SeatService;
import fa25.group.evtrainticket.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SeatLayoutController {

    private final SeatService seatService;
    private final ScheduleService scheduleService;

    @GetMapping("/seat-layout")
    public String showSeatLayout(@RequestParam("scheduleId") Integer scheduleId, Model model) {
        try {
            // Get schedule information
            var schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                model.addAttribute("error", "Không tìm thấy lịch trình tàu");
                return "error";
            }

            // Get seat layout data
            List<CarriageLayoutDto> carriageLayouts = seatService.getSeatLayout(scheduleId);

            // Prepare seat layout data for display
            for (CarriageLayoutDto carriage : carriageLayouts) {
                // Group seats by row for better layout
                Map<Integer, List<seatDto>> seatsByRow = carriage.getSeats()
                    .stream()
                    .collect(Collectors.groupingBy(seatDto::getRowNumber));

                // Sort rows and calculate max columns per row
                int maxColumns = seatsByRow.values().stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(4);

                carriage.setSeatsByRow(seatsByRow);
                carriage.setMaxColumns(maxColumns);
            }

            // Add data to model
            model.addAttribute("schedule", schedule);
            model.addAttribute("carriageLayouts", carriageLayouts);
            model.addAttribute("scheduleId", scheduleId);

            // Calculate summary statistics
            int totalSeats = carriageLayouts.stream()
                .mapToInt(c -> c.getSeats().size())
                .sum();

            long availableSeats = carriageLayouts.stream()
                .flatMap(c -> c.getSeats().stream())
                .filter(s -> "AVAILABLE".equals(s.getStatus().toString()))
                .count();

            long bookedSeats = totalSeats - availableSeats;

            model.addAttribute("totalSeats", totalSeats);
            model.addAttribute("availableSeats", availableSeats);
            model.addAttribute("bookedSeats", bookedSeats);

            return "booking/seat-layout";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải sơ đồ ghế: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/schedules")
    public String listSchedules(Model model) {
        try {
            var schedules = scheduleService.getAllSchedules();
            model.addAttribute("schedules", schedules);
            return "booking/schedule-list";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách lịch trình: " + e.getMessage());
            return "error";
        }
    }
}
