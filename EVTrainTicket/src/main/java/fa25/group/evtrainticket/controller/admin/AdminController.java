package fa25.group.evtrainticket.controller.admin;

import fa25.group.evtrainticket.dto.StatsResponse;
import fa25.group.evtrainticket.dto.WeeklyStatsResponse;
import fa25.group.evtrainticket.entity.*;
import fa25.group.evtrainticket.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TrainService trainService;
    private final CarriageTypeService carriageTypeService;
    private final CarriageService carriageService;
    private final SeatTypeService seatTypeService;
    private final SeatService seatService;
    private final StationService stationService;
    private final ScheduleService scheduleService;
    private final RouteService routeService;
    private final UserService userService;
    private final DashboardService dashboardService;

    // Helper kiểm tra quyền
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    // Helper tạo view chung
    private ModelAndView createAdminView(String contentFragment, String currentPage, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return new ModelAndView("redirect:/error"); // Trang lỗi 403
        }

        ModelAndView mav = new ModelAndView("admin/admin-layout");
        mav.addObject("user", user);
        mav.addObject("isAdmin", true);
        mav.addObject("currentPage", currentPage);
        mav.addObject("contentFragment", contentFragment);
        return mav;
    }

    @GetMapping("")
    public ModelAndView showAdminPage(HttpSession session) {
        ModelAndView mav = createAdminView("admin/dashboard", "admin", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        // Thêm thống kê cho dashboard
        mav.addObject("scheduleCount", scheduleService.getAllSchedules().size());
        mav.addObject("trainCount", trainService.getAllTrains().size());
        mav.addObject("stationCount", stationService.getAllStations().size());
        return mav;
    }

    // ================================== SCHEDULE (ĐÃ GỘP) =====================================

    @GetMapping("/schedules")
    public ModelAndView listSchedules(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/schedule/list", "schedules", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("schedules", scheduleService.getAllSchedules());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/schedules/new")
    public ModelAndView showCreateScheduleForm(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/schedule/form", "schedules", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("schedule", new Schedule());
        mav.addObject("stations", stationService.getAllStations());
        mav.addObject("routes", routeService.getAllRoutes());
//        mav.addObject("trains", trainService.getAllTrains());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/schedules/{id}/edit")
    public ModelAndView showEditScheduleForm(@PathVariable("id") Integer id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Schedule schedule = scheduleService.getScheduleById(id);
        if (schedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lịch trình với ID: " + id);
            return new ModelAndView("redirect:/admin/schedules");
        }

        ModelAndView mav = createAdminView("admin/schedule/form", "schedules", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("schedule", schedule);
        mav.addObject("stations", stationService.getAllStations());
        mav.addObject("routes", routeService.getAllRoutes());
//        mav.addObject("trains", trainService.getAllTrains());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/schedules/create")
    public String createSchedule(@RequestParam("departureStationId") Integer departureStationId,
                                 @RequestParam("arrivalStationId") Integer arrivalStationId,
                                 @RequestParam("departureDate") String departureDate,
                                 @RequestParam("departureTime") String departureTime,
                                 @RequestParam("arrivalDate") String arrivalDate,
                                 @RequestParam("arrivalTime") String arrivalTime,
                                 @RequestParam("distanceKm") Double distanceKm,
                                 @RequestParam("estimatedTime") Integer estimatedTime,
                                 @RequestParam("basePrice") BigDecimal basePrice,
                                 @RequestParam(value = "stopDuration", required = false) Integer stopDuration,
                                 @RequestParam(value = "createReturnTrip", required = false, defaultValue = "false") String createReturnTripStr,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        // Convert String to Boolean
        Boolean createReturnTrip = "true".equalsIgnoreCase(createReturnTripStr);
        if (!isAdmin(session)) return "redirect:/error";

        try {
            Station departureStation = stationService.getStationsByID(departureStationId);
            Station arrivalStation = stationService.getStationsByID(arrivalStationId);
//            Train train = trainService.getTrainById(trainId);

            if (departureStation == null || arrivalStation == null ) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ga không tồn tại.");
                return "redirect:/admin/schedules/new";
            }
            if (departureStationId.equals(arrivalStationId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ga đi và ga đến không được trùng nhau.");
                return "redirect:/admin/schedules/new";
            }

            LocalDateTime fullDepartureTime = LocalDateTime.parse(departureDate + "T" + departureTime);
            LocalDateTime fullArrivalTime = LocalDateTime.parse(arrivalDate + "T" + arrivalTime);

            // ===== KIỂM TRA NGÀY ĐI PHẢI TỪ NGÀY MAI TRỞ ĐI =====
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrowStart = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
            if (fullDepartureTime.isBefore(tomorrowStart)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ngày khởi hành phải từ ngày mai trở đi. Không được chọn hôm nay hoặc quá khứ.");
                return "redirect:/admin/schedules/new";
            }

            if (fullArrivalTime.isBefore(fullDepartureTime) || fullArrivalTime.isEqual(fullDepartureTime)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Giờ đến phải sau giờ đi.");
                return "redirect:/admin/schedules/new";
            }

            // ===== KIỂM TRA SCHEDULE TRÙNG LẶP =====
            List<Schedule> existingSchedules = scheduleService.getAllSchedules();
            boolean isDuplicate = existingSchedules.stream()
                    .anyMatch(s -> s.getDepartureStation().getStationID().equals(departureStationId)
                            && s.getArrivalStation().getStationID().equals(arrivalStationId)
                            && s.getDepartureTime().equals(fullDepartureTime));

            if (isDuplicate) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lịch trình này đã tồn tại! (Cùng ga đi, ga đến và thời gian khởi hành)");
                return "redirect:/admin/schedules/new";
            }

            // Tính toán khoảng cách & thời gian (support cả direct và indirect routes)
            java.util.Map<String, Object> routeData = routeService.calculateRouteDistance(departureStation, arrivalStation);

            if (routeData.get("distance_km") == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    (String) routeData.getOrDefault("error", "Không tìm thấy tuyến đường phù hợp."));
                return "redirect:/admin/schedules/new";
            }

            // Tính basePrice = distance × 700
            Double finalDistance = (Double) routeData.get("distance_km");
            BigDecimal finalPrice = BigDecimal.valueOf(Math.round(finalDistance * 700));

            Schedule schedule = new Schedule();

            // Set route nếu là direct (routeId != null)
            Long routeId = (Long) routeData.get("routeId");
            if (routeId != null) {
                Route directRoute = routeService.getRouteById(routeId);
                schedule.setRoute(directRoute);
            }

            schedule.setDepartureStation(departureStation);
            schedule.setArrivalStation(arrivalStation);
            schedule.setDepartureTime(fullDepartureTime);
            schedule.setArrivalTime(fullArrivalTime);
            schedule.setStopDuration(stopDuration);
            schedule.setBasePrice(finalPrice);

            scheduleService.saveSchedule(schedule);

            // ===== TỰ ĐỘNG TẠO LỊCH TRÌNH NGƯỢC LẠI (KHỨ HỒI) - NẾU ĐƯỢC CHỌN =====
            if (createReturnTrip) {
                try {
                    java.util.Map<String, Object> returnRouteData = routeService.calculateRouteDistance(arrivalStation, departureStation);

                    if (returnRouteData.get("distance_km") != null) {
                        Schedule returnSchedule = new Schedule();

                        Long returnRouteId = (Long) returnRouteData.get("routeId");
                        if (returnRouteId != null) {
                            Route returnDirectRoute = routeService.getRouteById(returnRouteId);
                            returnSchedule.setRoute(returnDirectRoute);
                        }

                        returnSchedule.setDepartureStation(arrivalStation);
                        returnSchedule.setArrivalStation(departureStation);

                        LocalDateTime returnDepartureTime = fullArrivalTime.plusHours(3);
                        returnSchedule.setDepartureTime(returnDepartureTime);

                        Double returnDistance = (Double) returnRouteData.get("distance_km");
                        Double defaultSpeed = 60.0;
                        int returnTravelMinutes = Math.round((float) ((returnDistance / defaultSpeed) * 60));
                        int returnTotalMinutes = returnTravelMinutes + (stopDuration != null ? stopDuration : 0);
                        LocalDateTime returnArrivalTime = returnDepartureTime.plusMinutes(returnTotalMinutes);
                        returnSchedule.setArrivalTime(returnArrivalTime);

                        BigDecimal returnPrice = BigDecimal.valueOf(Math.round(returnDistance * 700));
                        returnSchedule.setBasePrice(returnPrice);
                        returnSchedule.setStopDuration(stopDuration);

                        scheduleService.saveSchedule(returnSchedule);
                    }
                } catch (Exception e) {
                    System.err.println("Không thể tạo lịch trình khứ hồi: " + e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Tạo lịch trình thành công!");
            return "redirect:/admin/schedules";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo: " + e.getMessage());
            return "redirect:/admin/schedules/new";
        }
    }

    @PostMapping("/schedules/edit/{id}")
    public String updateSchedule(@PathVariable("id") Integer id,
                                 @RequestParam("departureStationId") Integer departureStationId,
                                 @RequestParam("arrivalStationId") Integer arrivalStationId,
                                 @RequestParam("departureDate") String departureDate,
                                 @RequestParam("departureTime") String departureTime,
                                 @RequestParam("arrivalDate") String arrivalDate,
                                 @RequestParam("arrivalTime") String arrivalTime,
                                 @RequestParam("distanceKm") Double distanceKm,
                                 @RequestParam("estimatedTime") Integer estimatedTime,
                                 @RequestParam("basePrice") BigDecimal basePrice,
                                 @RequestParam(value = "stopDuration", required = false) Integer stopDuration,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";

        try {
            Schedule existingSchedule = scheduleService.getScheduleById(id);
            if (existingSchedule == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lịch trình.");
                return "redirect:/admin/schedules";
            }

            Station departureStation = stationService.getStationsByID(departureStationId);
            Station arrivalStation = stationService.getStationsByID(arrivalStationId);
//            Train train = trainService.getTrainById(trainId);

            if (departureStation == null || arrivalStation == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ga không tồn tại.");
                return "redirect:/admin/schedules/" + id + "/edit";
            }
            if (departureStationId.equals(arrivalStationId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ga đi và ga đến không được trùng nhau.");
                return "redirect:/admin/schedules/" + id + "/edit";
            }

            LocalDateTime fullDepartureTime = LocalDateTime.parse(departureDate + "T" + departureTime);
            LocalDateTime fullArrivalTime = LocalDateTime.parse(arrivalDate + "T" + arrivalTime);

            // ===== KIỂM TRA NGÀY ĐI PHẢI TỪ NGÀY MAI TRỞ ĐI =====
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrowStart = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
            if (fullDepartureTime.isBefore(tomorrowStart)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ngày khởi hành phải từ ngày mai trở đi. Không được chọn hôm nay hoặc quá khứ.");
                return "redirect:/admin/schedules/" + id + "/edit";
            }

            if (fullArrivalTime.isBefore(fullDepartureTime) || fullArrivalTime.isEqual(fullDepartureTime)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Giờ đến phải sau giờ đi.");
                return "redirect:/admin/schedules/" + id + "/edit";
            }

            // ===== KIỂM TRA SCHEDULE TRÙNG LẶP (CHỈ TRONG CÁC SCHEDULE KHÁC) =====
            List<Schedule> existingSchedules = scheduleService.getAllSchedules();
            boolean isDuplicate = existingSchedules.stream()
                    .filter(s -> !s.getScheduleID().equals(id))  // Bỏ qua schedule hiện tại
                    .anyMatch(s -> s.getDepartureStation().getStationID().equals(departureStationId)
                            && s.getArrivalStation().getStationID().equals(arrivalStationId)
                            && s.getDepartureTime().equals(fullDepartureTime));

            if (isDuplicate) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lịch trình này đã tồn tại! (Cùng ga đi, ga đến và thời gian khởi hành)");
                return "redirect:/admin/schedules/" + id + "/edit";
            }

            // Tính toán khoảng cách & thời gian (support cả direct và indirect routes)
            java.util.Map<String, Object> routeData = routeService.calculateRouteDistance(departureStation, arrivalStation);

            if (routeData.get("distance_km") == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    (String) routeData.getOrDefault("error", "Không tìm thấy tuyến đường phù hợp."));
                return "redirect:/admin/schedules/" + id + "/edit";
            }

            // Set route nếu là direct (routeId != null)
            Long routeId = (Long) routeData.get("routeId");
            if (routeId != null) {
                Route directRoute = routeService.getRouteById(routeId);
                existingSchedule.setRoute(directRoute);
            } else {
                existingSchedule.setRoute(null);  // Indirect route
            }

            existingSchedule.setDepartureStation(departureStation);
            existingSchedule.setArrivalStation(arrivalStation);
//            existingSchedule.setTrain(train);
            existingSchedule.setDepartureTime(fullDepartureTime);
            existingSchedule.setArrivalTime(fullArrivalTime);
            existingSchedule.setStopDuration(stopDuration);

            // Tính basePrice = distance × 700
            Double finalDistance = (Double) routeData.get("distance_km");
            BigDecimal finalPrice = BigDecimal.valueOf(Math.round(finalDistance * 700));
            existingSchedule.setBasePrice(finalPrice);

            scheduleService.saveSchedule(existingSchedule);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lịch trình thành công!");
            return "redirect:/admin/schedules";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/schedules/" + id + "/edit";
        }
    }

    @GetMapping("/schedules/delete/{id}")
    public String deleteSchedule(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            scheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa lịch trình thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa thất bại! Lịch trình có thể đang được sử dụng.");
        }
        return "redirect:/admin/schedules";
    }

    // ================================== TRAIN =====================================

    @GetMapping("/train")
    public ModelAndView showTrainManagementPage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/train", "train", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("trainList", trainService.getAllTrains());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/train/create")
    public ModelAndView showCreateTrainPage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/create-train", "train", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("train", new Train());
        mav.addObject("scheduleList", scheduleService.getAllSchedules());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/train/create")
    public String createTrain(
            @RequestParam(value = "scheduleIds", required = false) List<Integer> scheduleIds,
            @ModelAttribute("train") Train train,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) return "redirect:/error";

        try {
            // Set schedules từ scheduleIds
            if (scheduleIds != null && !scheduleIds.isEmpty()) {
                List<Schedule> schedules = scheduleIds.stream()
                        .filter(Objects::nonNull) // lọc phần tử null
                        .map(id -> {
                            Schedule s = new Schedule();
                            s.setScheduleID(id);
                            return s;
                        })
                        .collect(Collectors.toList());
                train.setSchedules(schedules);
            }

            trainService.createTrain(train);
            redirectAttributes.addFlashAttribute("successMessage", "Chuyến tàu đã được thêm thành công");
            return "redirect:/admin/train";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("train", train);
            redirectAttributes.addFlashAttribute("selectedSchedules", scheduleIds);
            return "redirect:/admin/train/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm: " + e.getMessage());
            return "redirect:/admin/train/create";
        }
    }

    @GetMapping("/train/edit/{trainID}")
    public ModelAndView editTrain(HttpSession session, @PathVariable("trainID") Integer trainID, Model model, RedirectAttributes redirectAttributes) {
        Train trainToEdit = trainService.getTrainById(trainID);
        if (trainToEdit == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tàu ID: " + trainID);
            return new ModelAndView("redirect:/admin/train");
        }

        ModelAndView mav = createAdminView("admin/edit-train", "train", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("train", trainToEdit);
        mav.addObject("scheduleList", scheduleService.getAllSchedules());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/train/edit")
    public String editTrain(
            @RequestParam(value = "scheduleIds", required = false) List<Integer> scheduleIds,
            @ModelAttribute("train") Train train,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) return "redirect:/error";

        try {
            // Set schedules từ scheduleIds
            if(scheduleIds != null && !scheduleIds.isEmpty()) {
                List<Schedule> schedules = scheduleIds.stream()
                        .filter(Objects::nonNull) // lọc phần tử null
                        .map(id -> {
                            Schedule s = new Schedule();
                            s.setScheduleID(id);
                            return s;
                        })
                        .collect(Collectors.toList());
                train.setSchedules(schedules);
            }

            trainService.updateTrain(train.getTrainID(), train);
            redirectAttributes.addFlashAttribute("successMessage", "Chuyến tàu đã được cập nhật thành công");
            return "redirect:/admin/train";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/train/edit/" + train.getTrainID();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/train/edit/" + train.getTrainID();
        }
    }

    @GetMapping("/train/delete/{trainID}")
    public String deleteTrain(@PathVariable("trainID") Integer trainID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            trainService.deleteTrain(trainID);
            redirectAttributes.addFlashAttribute("successMessage", "Chuyến tàu đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa tàu: " + e.getMessage());
        }
        return "redirect:/admin/train";
    }

    /**
     * ================================== CARRIAGE TYPE =====================================
     */
    @GetMapping("/carriageTypes")
    public ModelAndView showCarriageTypeManagementPage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/carriageTypes", "carriageTypes", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/carriageTypes/delete/{carriageTypeID}")
    public String deleteCarriageType(@PathVariable("carriageTypeID") Integer carriageTypeID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            carriageTypeService.deleteCarriageType(carriageTypeID);
            redirectAttributes.addFlashAttribute("successMessage", "Loại toa đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa loại toa: " + e.getMessage());
        }
        return "redirect:/admin/carriageTypes";
    }

    @GetMapping("/carriageTypes/create")
    public ModelAndView createCarriageType(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/create-carriageType", "carriageTypes", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("carriageType", new CarriageType());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/carriageTypes/create")
    public String createCarriageType(@ModelAttribute("carriageType") CarriageType carriageType, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            carriageTypeService.createCarriageType(carriageType);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm loại toa mới thành công");
            return "redirect:/admin/carriageTypes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm: " + e.getMessage());
            return "redirect:/admin/carriageTypes/create";
        }
    }

    @GetMapping("/carriageTypes/edit/{carriageTypeId}")
    public ModelAndView editCarriageType(@PathVariable("carriageTypeId") Integer carriageTypeId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        CarriageType carriageTypeToEdit = carriageTypeService.findCarriageTypeById(carriageTypeId);
        if (carriageTypeToEdit == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy loại toa ID: " + carriageTypeId);
            return new ModelAndView("redirect:/admin/carriageTypes");
        }

        ModelAndView mav = createAdminView("admin/edit-carriageType", "carriageTypes", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("carriageType", carriageTypeToEdit);
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/carriageTypes/edit")
    public String editCarriageType(@ModelAttribute("carriageType") CarriageType carriageType, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            carriageTypeService.updateCarriageType(carriageType.getCarriageTypeId(), carriageType);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật loại toa thành công");
            return "redirect:/admin/carriageTypes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/carriageTypes/edit/" + carriageType.getCarriageTypeId();
        }
    }

    /**
     * ======================= CARRIAGE ==================================
     */
    @GetMapping("/carriages")
    public ModelAndView getAllCarriage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/carriages", "carriages", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("carriageList", carriageService.findAllCarriages());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/carriages/create")
    public ModelAndView createCarriage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/create-carriage", "carriages", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("carriage", new Carriage());
        mav.addObject("trainList", trainService.getAllTrains());
        mav.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/carriages/create")
    public String createCarriage(@ModelAttribute("carriage") Carriage carriage, @RequestParam("trainID") Integer trainId, @RequestParam("carriageTypeId") Integer carriageTypeId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            Train trainToAdd = trainService.getTrainById(trainId);
            CarriageType carriageTypeToAdd = carriageTypeService.findCarriageTypeById(carriageTypeId);
            carriage.setTrain(trainToAdd);
            carriage.setCarriageType(carriageTypeToAdd);

            if (carriageTypeToAdd != null) {
                carriage.setTotalSeats(carriageTypeToAdd.getSeatCount());
            } else {
                // Phòng trường hợp carriageTypeToAdd bị null
                // (Mặc dù nếu bị null, nó sẽ báo lỗi ở dòng trên,
                // nhưng đây là cách code an toàn)
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Loại toa không hợp lệ.");
                return "redirect:/admin/carriages/create";
            }

            carriageService.saveCarriage(carriage);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm toa tàu mới thành công");
            return "redirect:/admin/carriages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm: " + e.getMessage());
            return "redirect:/admin/carriages/create";
        }
    }

    @GetMapping("/carriages/edit/{carriageID}")
    public ModelAndView editCarriage(@PathVariable("carriageID") Integer carriageID, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Carriage carriage = carriageService.findCarriageById(carriageID);
        if (carriage == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy toa ID: " + carriageID);
            return new ModelAndView("redirect:/admin/carriages");
        }

        ModelAndView mav = createAdminView("admin/edit-carriage", "carriages", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("carriage", carriage);
        mav.addObject("trainList", trainService.getAllTrains());
        mav.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/carriages/edit")
    public String editCarriage(@ModelAttribute("carriage") Carriage carriage, @RequestParam("trainID") Integer trainId, @RequestParam("carriageTypeId") Integer carriageTypeId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            Train trainToAdd = trainService.getTrainById(trainId);
            CarriageType carriageTypeToAdd = carriageTypeService.findCarriageTypeById(carriageTypeId);
            carriage.setTrain(trainToAdd);
            carriage.setCarriageType(carriageTypeToAdd);
            carriageService.updateCarriage(carriage.getCarriageID(), carriage);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật toa tàu thành công");
            return "redirect:/admin/carriages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/carriages/edit/" + carriage.getCarriageID();
        }
    }

    @GetMapping("/carriages/delete/{carriageID}")
    public String deleteCarriage(@PathVariable("carriageID") Integer carriageID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            carriageService.deleteCarriage(carriageID);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa toa tàu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa toa tàu thất bại: " + e.getMessage());
        }
        return "redirect:/admin/carriages";
    }

    /**
     * =================== SEAT TYPE =================================================
     */

    @GetMapping("/seatTypes")
    public ModelAndView showSeatTypeManagementPage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/seatTypes", "seatTypes", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/seatTypes/delete/{seatTypeId}")
    public String deleteSeatType(@PathVariable("seatTypeId") Integer seatTypeIdID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            seatTypeService.deleteSeatType(seatTypeIdID);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa loại ghế thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa loại ghế: " + e.getMessage());
        }
        return "redirect:/admin/seatTypes";
    }

    @GetMapping("/seatTypes/create")
    public ModelAndView createSeatType(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/create-seatType", "seatTypes", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("seatType", new SeatType());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/seatTypes/create")
    public String createSeatType(@ModelAttribute("seatType") SeatType seatType, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            seatTypeService.saveSeatType(seatType);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm loại ghế mới thành công");
            return "redirect:/admin/seatTypes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm: " + e.getMessage());
            return "redirect:/admin/seatTypes/create";
        }
    }

    @GetMapping("/seatTypes/edit/{seatTypesID}")
    public ModelAndView editSeatType(@PathVariable("seatTypesID") Integer seatTypesID, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        SeatType seatType = seatTypeService.getSeatTypeById(seatTypesID);
        if (seatType == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy loại ghế ID: " + seatTypesID);
            return new ModelAndView("redirect:/admin/seatTypes");
        }

        ModelAndView mav = createAdminView("admin/edit-seatType", "seatTypes", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("seatType", seatType);
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/seatTypes/edit")
    public String editSeatType(@ModelAttribute("seatType") SeatType seatType, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            seatTypeService.updateSeatType(seatType.getSeatTypeID(), seatType);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật loại ghế thành công");
            return "redirect:/admin/seatTypes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/seatTypes/edit/" + seatType.getSeatTypeID();
        }
    }

    /*====================== SEAT ============================*/

    @GetMapping("/seats")
    public ModelAndView showSeatManagementPage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/seats", "seats", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("seatList", seatService.getAllSeats());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/seats/delete/{seatID}")
    public String deleteSeat(@PathVariable("seatID") Integer seatID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            seatService.deleteSeat(seatID);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa ghế tàu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa ghế: " + e.getMessage());
        }
        return "redirect:/admin/seats";
    }

    @GetMapping("/seats/create")
    public ModelAndView createSeatPage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/create-seat", "seats", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("seat", new Seat());
        mav.addObject("carriageList", carriageService.findAllCarriages());
        mav.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/seats/create")
    public String createSeat(@ModelAttribute("seat") Seat seat,
                             @RequestParam("carriage.carriageID") Integer carriageID,
                             @RequestParam("seatType.seatTypeID") Integer seatTypeID,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            Carriage carriage = carriageService.findCarriageById(carriageID);
            SeatType seatType = seatTypeService.getSeatTypeById(seatTypeID);
            seat.setCarriage(carriage);
            seat.setSeatType(seatType);
            seatService.saveSeat(seat);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo ghế mới thành công");
            return "redirect:/admin/seats";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo ghế: " + e.getMessage());
            return "redirect:/admin/seats/create";
        }
    }

    @GetMapping("/seats/edit/{seatID}")
    public ModelAndView editSeat(@PathVariable("seatID") Integer seatID, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Seat seat = seatService.getSeatById(seatID);
        if (seat == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ghế ID: " + seatID);
            return new ModelAndView("redirect:/admin/seats");
        }

        ModelAndView mav = createAdminView("admin/edit-seat", "seats", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("seat", seat);
        mav.addObject("carriageList", carriageService.findAllCarriages());
        mav.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/seats/edit")
    public String editSeat(@ModelAttribute("seat") Seat seat,
                           @RequestParam("carriage.carriageID") Integer carriageID,
                           @RequestParam("seatType.seatTypeID") Integer seatTypeID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            Carriage carriage = carriageService.findCarriageById(carriageID);
            SeatType seatType = seatTypeService.getSeatTypeById(seatTypeID);
            seat.setCarriage(carriage);
            seat.setSeatType(seatType);
            seatService.updateSeat(seat.getSeatID(), seat);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ghế thành công");
            return "redirect:/admin/seats";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật ghế: " + e.getMessage());
            return "redirect:/admin/seats/edit/" + seat.getSeatID();
        }
    }

    /* ========================== STATION ========================================== */

    @GetMapping("/stations")
    public ModelAndView showStationManagementPage(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/stations", "stations", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("stationList", stationService.getAllStations());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/stations/delete/{stationID}")
    public String deleteStation(@PathVariable("stationID") Integer stationID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            stationService.deleteStation(stationID);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa ga tàu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa ga tàu: " + e.getMessage());
        }
        return "redirect:/admin/stations";
    }

    @GetMapping("/stations/create")
    public ModelAndView createStation(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/create-station", "stations", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("station", new Station());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/stations/create")
    public String createStation(@ModelAttribute("station") Station station, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            stationService.saveStation(station);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm ga tàu mới thành công");
            return "redirect:/admin/stations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm: " + e.getMessage());
            return "redirect:/admin/stations/create";
        }
    }

    @GetMapping("/stations/edit/{stationID}")
    public ModelAndView editStation(@PathVariable("stationID") Integer stationID, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Station station = stationService.getStationsByID(stationID);
        if (station == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ga ID: " + stationID);
            return new ModelAndView("redirect:/admin/stations");
        }

        ModelAndView mav = createAdminView("admin/edit-station", "stations", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("station", station);
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/stations/edit")
    public String editStation(@ModelAttribute("station") Station station, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            stationService.updateStation(station.getStationID(), station);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ga tàu thành công");
            return "redirect:/admin/stations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/stations/edit/" + station.getStationID();
        }
    }

    /*====================== USER ============================*/
    @GetMapping("/users")
    public ModelAndView showUser(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/users", "users", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("userList", userService.getAllUsers());
        mav.addObject("successMessage", model.asMap().get("successMessage"));
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @GetMapping("/users/create")
    public ModelAndView createUser(HttpSession session, Model model) {
        ModelAndView mav = createAdminView("admin/create-user", "users", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("user", new User());
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute("user") User user, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nguời dùng mới thành công");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm: " + e.getMessage());
            return "redirect:/admin/users/create";
        }
    }

    @GetMapping("/users/edit/{userID}")
    public ModelAndView editUser(@PathVariable("userID") Integer userID, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findById(userID);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng ID: " + userID);
            return new ModelAndView("redirect:/admin/users");
        }

        ModelAndView mav = createAdminView("admin/edit-user", "users", session);
        if (mav.getViewName().startsWith("redirect")) return mav;

        mav.addObject("user", user);
        mav.addObject("errorMessage", model.asMap().get("errorMessage"));
        return mav;
    }

    @PostMapping("/users/edit")
    public String editUser(@ModelAttribute("user") User user, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            userService.updateUser(user.getUserID(),  user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/users/edit/" + user.getUserID();
        }
    }

    @GetMapping("/users/delete/{userID}")
    public String deleteUser(@PathVariable("userID") Integer userID, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/error";
        try {
            userService.deleteUser(userID);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /*====================== DASHBOARD ============================*/
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        model.addAttribute("startMonth", previousMonth.toString());
        model.addAttribute("endMonth", currentMonth.toString());

        return "admin/dashboard";
    }

    @GetMapping("/dashboard/total-stats")
    @ResponseBody
    public ResponseEntity<List<StatsResponse>> getTotalStats() {
        List<StatsResponse> stats = dashboardService.getAllStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/weekly-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWeeklyStats(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {

        YearMonth startYearMonth;
        YearMonth endYearMonth;

        if (startMonth == null || startMonth.isBlank()) {
            startYearMonth = YearMonth.now();
        } else {
            startYearMonth = YearMonth.parse(startMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        if (endMonth == null || endMonth.isBlank()) {
            endYearMonth = YearMonth.now();
        } else {
            endYearMonth = YearMonth.parse(endMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        LocalDate startDate = startYearMonth.atDay(1);
        LocalDate endDate = endYearMonth.atEndOfMonth();

        List<WeeklyStatsResponse> weeklyStats = dashboardService.getAllStatsWeekly(startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("weeklyStats", weeklyStats);
        response.put("dateRange", startDate + " -> " + endDate);
        response.put("monthRange", startMonth + " - " + endMonth);

        return ResponseEntity.ok(response);
    }

}