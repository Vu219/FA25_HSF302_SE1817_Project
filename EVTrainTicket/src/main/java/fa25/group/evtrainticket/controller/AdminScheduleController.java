package fa25.group.evtrainticket.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.entity.Train;
import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.service.ScheduleService;
import fa25.group.evtrainticket.service.StationService;
import fa25.group.evtrainticket.service.TrainService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/schedules")
public class AdminScheduleController {

    private final ScheduleService scheduleService;
    private final StationService stationService;
    private final TrainService trainService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping
    public String listSchedules(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/home?error=unauthorized";
        }
        List<Schedule> schedules = scheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);
        model.addAttribute("user", session.getAttribute("user"));
        return "admin/schedule/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/home?error=unauthorized";
        }
        model.addAttribute("schedule", new Schedule());
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("user", session.getAttribute("user"));
        return "admin/schedule/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Integer id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/home?error=unauthorized";
        }
        Schedule schedule = scheduleService.getScheduleById(id);
        if (schedule == null) {
            return "redirect:/admin/schedules?error=scheduleNotFound";
        }
        model.addAttribute("schedule", schedule);
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("user", session.getAttribute("user"));
        return "admin/schedule/form";
    }

    // CREATE - POST /admin/schedules
    @PostMapping
    public String createSchedule(@RequestParam("departureStationId") Integer departureStationId, 
                                 @RequestParam("arrivalStationId") Integer arrivalStationId,
                                 @RequestParam("departureDate") String departureDate,
                                 @RequestParam("departureTime") String departureTime,
                                 @RequestParam("arrivalDate") String arrivalDate,
                                 @RequestParam("arrivalTime") String arrivalTime,
                                 @RequestParam("train.id") Integer trainId,
                                 @RequestParam("distanceKm") Double distanceKm,
                                 @RequestParam("estimatedTime") Integer estimatedTime,
                                 @RequestParam("basePrice") Double basePrice,
                                 @RequestParam("status") String status,
                                 HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/home?error=unauthorized";
        }
        
        try {
            Station departureStation = stationService.getStationById(departureStationId);
            Station arrivalStation = stationService.getStationById(arrivalStationId);
            Train train = trainService.getTrainById(trainId);

            if (departureStation == null || arrivalStation == null || train == null) {
                return "redirect:/admin/schedules/new?error=stationOrTrainNotFound";
            }

            if (departureStationId.equals(arrivalStationId)) {
                return "redirect:/admin/schedules/new?error=sameStation";
            }

            LocalDateTime fullDepartureTime = LocalDateTime.parse(departureDate + "T" + departureTime);
            LocalDateTime fullArrivalTime = LocalDateTime.parse(arrivalDate + "T" + arrivalTime);

            if (fullArrivalTime.isBefore(fullDepartureTime) || fullArrivalTime.isEqual(fullDepartureTime)) {
                return "redirect:/admin/schedules/new?error=invalidTime";
            }

            Schedule schedule = new Schedule();
            schedule.setDepartureStation(departureStation);
            schedule.setArrivalStation(arrivalStation);
            schedule.setTrain(train);
            schedule.setDepartureTime(fullDepartureTime);
            schedule.setArrivalTime(fullArrivalTime);
            schedule.setDistanceKm(distanceKm);
            schedule.setEstimatedTime(estimatedTime);
            schedule.setBasePrice(basePrice);
            schedule.setStatus(status);
            
            scheduleService.saveSchedule(schedule);
            return "redirect:/admin/schedules?success=scheduleCreated";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/schedules/new?error=" + e.getMessage();
        }
    }

    // UPDATE - PUT /admin/schedules/{id}
    @PutMapping("/{id}")
    public String updateSchedule(@PathVariable("id") Integer id,
                                 @RequestParam("departureStationId") Integer departureStationId, 
                                 @RequestParam("arrivalStationId") Integer arrivalStationId,
                                 @RequestParam("departureDate") String departureDate,
                                 @RequestParam("departureTime") String departureTime,
                                 @RequestParam("arrivalDate") String arrivalDate,
                                 @RequestParam("arrivalTime") String arrivalTime,
                                 @RequestParam("train.id") Integer trainId,
                                 @RequestParam("distanceKm") Double distanceKm,
                                 @RequestParam("estimatedTime") Integer estimatedTime,
                                 @RequestParam("basePrice") Double basePrice,
                                 @RequestParam("status") String status,
                                 HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/home?error=unauthorized";
        }
        
        try {
            Schedule existingSchedule = scheduleService.getScheduleById(id);
            if (existingSchedule == null) {
                return "redirect:/admin/schedules?error=scheduleNotFound";
            }

            Station departureStation = stationService.getStationById(departureStationId);
            Station arrivalStation = stationService.getStationById(arrivalStationId);
            Train train = trainService.getTrainById(trainId);

            if (departureStation == null || arrivalStation == null || train == null) {
                return "redirect:/admin/schedules/" + id + "/edit?error=stationOrTrainNotFound";
            }

            if (departureStationId.equals(arrivalStationId)) {
                return "redirect:/admin/schedules/" + id + "/edit?error=sameStation";
            }

            LocalDateTime fullDepartureTime = LocalDateTime.parse(departureDate + "T" + departureTime);
            LocalDateTime fullArrivalTime = LocalDateTime.parse(arrivalDate + "T" + arrivalTime);

            if (fullArrivalTime.isBefore(fullDepartureTime) || fullArrivalTime.isEqual(fullDepartureTime)) {
                return "redirect:/admin/schedules/" + id + "/edit?error=invalidTime";
            }

            existingSchedule.setDepartureStation(departureStation);
            existingSchedule.setArrivalStation(arrivalStation);
            existingSchedule.setTrain(train);
            existingSchedule.setDepartureTime(fullDepartureTime);
            existingSchedule.setArrivalTime(fullArrivalTime);
            existingSchedule.setDistanceKm(distanceKm);
            existingSchedule.setEstimatedTime(estimatedTime);
            existingSchedule.setBasePrice(basePrice);
            existingSchedule.setStatus(status);
            
            scheduleService.saveSchedule(existingSchedule);
            return "redirect:/admin/schedules?success=scheduleUpdated";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/schedules/" + id + "/edit?error=" + e.getMessage();
        }
    }

    // DELETE - DELETE /admin/schedules/{id}
    @DeleteMapping("/{id}")
    public String deleteSchedule(@PathVariable("id") Integer id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/home?error=unauthorized";
        }
        try {
            scheduleService.deleteSchedule(id);
            return "redirect:/admin/schedules?success=scheduleDeleted";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/schedules?error=deleteFailed";
        }
    }
}