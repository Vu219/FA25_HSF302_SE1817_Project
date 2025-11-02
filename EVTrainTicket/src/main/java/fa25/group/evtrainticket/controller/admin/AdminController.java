package fa25.group.evtrainticket.controller.admin;

import fa25.group.evtrainticket.entity.*;
import fa25.group.evtrainticket.service.*;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    private final UserService userService;

    @GetMapping("")
    public ModelAndView showAdminPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "admin");
        return modelAndView;
    }

    @GetMapping("/dashboard")
    public String showAdminPage() {
        return "admin-2/dashboard";
    }

    @GetMapping("/train")
    public ModelAndView showTrainManagementPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "train");
        modelAndView.addObject("contentFragment", "admin/train");
        modelAndView.addObject("trainList", trainService.getAllTrains());
        return modelAndView;
    }

    @GetMapping("/train/create")
    public ModelAndView showCreateTrainPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "train");
        modelAndView.addObject("contentFragment", "admin/create-train");
        modelAndView.addObject("train", new Train());
        return modelAndView;
    }

    @PostMapping("/train/create")
    public ModelAndView createTrain(@ModelAttribute("train") Train train, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "train");
        modelAndView.addObject("contentFragment", "admin/create-train");
        try {
            trainService.createTrain(train);
            modelAndView.addObject("successMessage", "Chuyến tàu đã được thêm thành công");
            modelAndView.addObject("contentFragment", "admin/train");
            modelAndView.addObject("trainList", trainService.getAllTrains());
        } catch (Exception e) {
            modelAndView.addObject("train", train);
            modelAndView.addObject("errorMessage", "Lỗi khi thêm chuyến tàu: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/train/edit/{trainID}")
    public ModelAndView editTrain(HttpSession session, @PathVariable("trainID") Integer trainID) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "train");
        modelAndView.addObject("contentFragment", "admin/edit-train");
        Train trainToEdit = trainService.getTrainById(trainID);
        modelAndView.addObject("train", trainToEdit);
        return modelAndView;
    }

    @PostMapping("/train/edit")
    public ModelAndView editTrain(@ModelAttribute("train") Train train, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "train");
        modelAndView.addObject("contentFragment", "admin/edit-train");
        try {
            trainService.updateTrain(train.getTrainID(), train);
            modelAndView.addObject("successMessage", "Chuyến tàu đã được cập nhật thành công");
            modelAndView.addObject("contentFragment", "admin/train");
            modelAndView.addObject("trainList", trainService.getAllTrains());
        } catch (Exception e) {
            modelAndView.addObject("train", train);
            modelAndView.addObject("errorMessage", "Lỗi khi cập nhật chuyến tàu: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/train/delete/{trainID}")
    public ModelAndView deleteTrain(@PathVariable("trainID") Integer trainID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "train");
        modelAndView.addObject("contentFragment", "admin/train");

        try {
            trainService.deleteTrain(trainID);
            modelAndView.addObject("trainList", trainService.getAllTrains());
            modelAndView.addObject("successMessage", "Chuyến tàu đã được xóa thành công");
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Lỗi khi xóa chuyến tàu: " + e.getMessage());
        }

        return modelAndView;
    }

    /**
     * ================================== CARRIAGE TYPE
     * =====================================
     */
    @GetMapping("/carriageTypes")
    public ModelAndView showCarriageManagementPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriageTypes");
        modelAndView.addObject("contentFragment", "admin/carriageTypes");
        modelAndView.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        return modelAndView;
    }

    @GetMapping("/carriageTypes/delete/{carriageTypeID}")
    public ModelAndView deleteCarriageType(@PathVariable("carriageTypeID") Integer carriageTypeID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriageTypes");
        modelAndView.addObject("contentFragment", "admin/carriageTypes");

        try {
            carriageTypeService.deleteCarriageType(carriageTypeID);
            modelAndView.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
            modelAndView.addObject("successMessage", "Loại toa đã được xóa thành công");
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Lỗi khi xóa loại toa: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/carriageTypes/create")
    public ModelAndView createCarriageType(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriageTypes");
        modelAndView.addObject("contentFragment", "admin/create-carriageType");
        modelAndView.addObject("carriageType", new CarriageType());
        return modelAndView;
    }

    @PostMapping("/carriageTypes/create")
    public ModelAndView createCarriageType(@ModelAttribute("carriageType") CarriageType carriageType, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriageTypes");
        modelAndView.addObject("contentFragment", "admin/create-carriageType");

        try {
            carriageTypeService.createCarriageType(carriageType);
            modelAndView.addObject("successMessage", "Thêm loại toa mới thành công");
            modelAndView.addObject("contentFragment", "admin/carriageTypes");
            modelAndView.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        } catch (Exception e) {
            modelAndView.addObject("carriageType", carriageType);
            modelAndView.addObject("errorMessage", "Lỗi khi thêm loại toa mới: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/carriageTypes/edit/{carriageTypeId}")
    public ModelAndView editCarriageType(@PathVariable("carriageTypeId") Integer carriageTypeId, HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriageTypes");
        modelAndView.addObject("contentFragment", "admin/edit-carriageType");
        CarriageType carriageTypeToEdit = carriageTypeService.findCarriageTypeById(carriageTypeId);
        modelAndView.addObject("carriageType", carriageTypeToEdit);
        return modelAndView;
    }

    @PostMapping("/carriageTypes/edit")
    public ModelAndView editCarriageType(@ModelAttribute("carriageType") CarriageType carriageType, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriageTypes");
        modelAndView.addObject("contentFragment", "admin/edit-carriageType");

        try {
            carriageTypeService.updateCarriageType(carriageType.getCarriageTypeId(), carriageType);
            modelAndView.addObject("successMessage", "Cập nhật loại toa thành công");
            modelAndView.addObject("contentFragment", "admin/carriageTypes");
            modelAndView.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        } catch (Exception e) {
            modelAndView.addObject("carriageType", carriageType);
            modelAndView.addObject("errorMessage", "Lỗi khi cập nhật loại toa: " + e.getMessage());
        }

        return modelAndView;
    }

    /**
     * ======================= CARRIAGE ==================================
     */
    @GetMapping("/carriages")
    public ModelAndView getAllCarriage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriages");
        modelAndView.addObject("contentFragment", "admin/carriages");
        modelAndView.addObject("carriageList", carriageService.findAllCarriages());
        return modelAndView;
    }

    @GetMapping("/carriages/create")
    public ModelAndView createCarriage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriages");
        modelAndView.addObject("contentFragment", "admin/create-carriage");
        modelAndView.addObject("carriage", new Carriage());
        modelAndView.addObject("trainList", trainService.getAllTrains());
        modelAndView.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        return modelAndView;
    }

    @PostMapping("/carriages/create")
    public ModelAndView createCarriage(@ModelAttribute("carriage") Carriage carriage, @RequestParam("trainID") Integer trainId, @RequestParam("carriageTypeId") Integer carriageTypeId, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriages");
        modelAndView.addObject("contentFragment", "admin/create-carriage");

        try {
            Train trainToAdd = trainService.getTrainById(trainId);
            carriage.setTrain(trainToAdd);
            CarriageType carriageTypeToAdd = carriageTypeService.findCarriageTypeById(carriageTypeId);
            carriage.setCarriageType(carriageTypeToAdd);
            carriageService.saveCarriage(carriage);
            modelAndView.addObject("successMessage", "Thêm toa tàu mới thành công");
            modelAndView.addObject("contentFragment", "admin/carriages");
            modelAndView.addObject("carriageList", carriageService.findAllCarriages());
        } catch (Exception e) {
            modelAndView.addObject("carriage", carriage);
            modelAndView.addObject("errorMessage", "Lỗi khi thêm toa tàu mới: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/carriages/edit/{carriageID}")
    public ModelAndView editCarriage(@PathVariable("carriageID") Integer carriageID, HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriages");
        modelAndView.addObject("contentFragment", "admin/edit-carriage");
        modelAndView.addObject("carriage", carriageService.findCarriageById(carriageID));
        modelAndView.addObject("trainList", trainService.getAllTrains());
        modelAndView.addObject("carriageTypeList", carriageTypeService.findAllCarriageTypes());
        return modelAndView;
    }

    @PostMapping("/carriages/edit")
    public ModelAndView editCarriage(@ModelAttribute("carriage") Carriage carriage, @RequestParam("trainID") Integer trainId, @RequestParam("carriageTypeId") Integer carriageTypeId, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriages");
        modelAndView.addObject("contentFragment", "admin/edit-carriage");

        try {
            Train trainToAdd = trainService.getTrainById(trainId);
            carriage.setTrain(trainToAdd);
            CarriageType carriageTypeToAdd = carriageTypeService.findCarriageTypeById(carriageTypeId);
            carriage.setCarriageType(carriageTypeToAdd);
            carriageService.updateCarriage(carriage.getCarriageID(), carriage);
            modelAndView.addObject("successMessage", "Cập nhật toa tàu mới thành công");
            modelAndView.addObject("contentFragment", "admin/carriages");
            modelAndView.addObject("carriageList", carriageService.findAllCarriages());
        } catch (Exception e) {
            modelAndView.addObject("carriage", carriage);
            modelAndView.addObject("carriageList", carriageService.findAllCarriages());
            modelAndView.addObject("trainList", trainService.getAllTrains());
            modelAndView.addObject("errorMessage", "Lỗi khi cập nhật toa tàu: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/carriages/delete/{carriageID}")
    public ModelAndView deleteCarriage(@PathVariable("carriageID") Integer carriageID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "carriages");
        modelAndView.addObject("contentFragment", "admin/carriages");

        try {
            carriageService.deleteCarriage(carriageID);
            modelAndView.addObject("carriageList", carriageService.findAllCarriages());
            modelAndView.addObject("successMessage", "Xóa toa tàu thành công");
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Xóa toa tàu thất bại");
        }
        return modelAndView;
    }

    /**
     * =================== SEAT TYPE =================================================
     */

    @GetMapping("/seatTypes")
    public ModelAndView showSeatTypeManagementPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seatTypes");
        modelAndView.addObject("contentFragment", "admin/seatTypes");
        modelAndView.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        return modelAndView;
    }

    @GetMapping("/seatTypes/delete/{seatTypeId}")
    public ModelAndView deleteSeatType(@PathVariable("seatTypeId") Integer seatTypeIdID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seatTypes");
        modelAndView.addObject("contentFragment", "admin/seatTypes");

        try {
            seatTypeService.deleteSeatType(seatTypeIdID);
            modelAndView.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
            modelAndView.addObject("successMessage", "Xóa loại ghế thành công");
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Lỗi khi xóa loại ghế: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/seatTypes/create")
    public ModelAndView createSeatType(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seatTypes");
        modelAndView.addObject("contentFragment", "admin/create-seatType");
        modelAndView.addObject("seatType", new SeatType());
        return modelAndView;
    }

    @PostMapping("/seatTypes/create")
    public ModelAndView createSeatType(@ModelAttribute("seatType") SeatType seatType, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seatTypes");
        modelAndView.addObject("contentFragment", "admin/create-seatType");

        try {
            seatTypeService.saveSeatType(seatType);
            modelAndView.addObject("successMessage", "Thêm loại ghế mới thành công");
            modelAndView.addObject("contentFragment", "admin/seatTypes");
            modelAndView.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        } catch (Exception e) {
            modelAndView.addObject("seatType", seatType);
            modelAndView.addObject("errorMessage", "Lỗi khi thêm loại ghế mới: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/seatTypes/edit/{seatTypesID}")
    public ModelAndView editSeatType(@PathVariable("seatTypesID") Integer seatTypesID, HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seatTypes");
        modelAndView.addObject("contentFragment", "admin/edit-seatType");
        modelAndView.addObject("seatType", seatTypeService.getSeatTypeById(seatTypesID));
        return modelAndView;
    }

    @PostMapping("/seatTypes/edit")
    public ModelAndView editSeatType(@ModelAttribute("seatType") SeatType seatType, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seatTypes");
        modelAndView.addObject("contentFragment", "admin/edit-seatType");

        try {
            seatTypeService.updateSeatType(seatType.getSeatTypeID(), seatType);
            modelAndView.addObject("successMessage", "Cập nhật loại ghế thành công");
            modelAndView.addObject("contentFragment", "admin/seatTypes");
            modelAndView.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        } catch (Exception e) {
            modelAndView.addObject("seatType", seatType);
            modelAndView.addObject("errorMessage", "Lỗi khi cập nhật loại ghế: " + e.getMessage());
        }
        return modelAndView;
    }

    /*====================== SEAT ============================*/

    @GetMapping("/seats")
    public ModelAndView showSeatManagementPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seats");
        modelAndView.addObject("contentFragment", "admin/seats");
        modelAndView.addObject("seatList", seatService.getAllSeats());
        return modelAndView;
    }

    @GetMapping("/seats/delete/{seatID}")
    public ModelAndView deleteSeat(@PathVariable("seatID") Integer seatID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seats");
        modelAndView.addObject("contentFragment", "admin/seats");

        try {
            seatService.deleteSeat(seatID);
            modelAndView.addObject("seatList", seatService.getAllSeats());
            modelAndView.addObject("successMessage", "Xóa ghế tàu thành công");
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Lỗi khi xóa ghê tàu: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/seats/create")
    public ModelAndView createSeatPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seats");
        modelAndView.addObject("contentFragment", "admin/create-seat");
        modelAndView.addObject("seat", new Seat());
        modelAndView.addObject("carriageList", carriageService.findAllCarriages());
        modelAndView.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        return modelAndView;
    }

    @PostMapping("/seats/create")
    public ModelAndView createSeat(@ModelAttribute("seat") Seat seat,
                                   @RequestParam("carriage.carriageID") Integer carriageID,
                                   @RequestParam("seatType.seatTypeID") Integer seatTypeID,
                                   HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seats");
        modelAndView.addObject("contentFragment", "admin/create-seat");

        try {
            Carriage carriage = carriageService.findCarriageById(carriageID);
            SeatType seatType = seatTypeService.getSeatTypeById(seatTypeID);
            seat.setCarriage(carriage);
            seat.setSeatType(seatType);
            seatService.saveSeat(seat);
            modelAndView.addObject("successMessage", "Tạo ghế mới thành công");
            return new ModelAndView("redirect:/admin/seats");

        } catch (Exception e) {
            modelAndView.addObject("seat", seat);
            modelAndView.addObject("carriageList", carriageService.findAllCarriages());
            modelAndView.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
            modelAndView.addObject("errorMessage", "Lỗi khi tạo ghế mới: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/seats/edit/{seatID}")
    public ModelAndView editSeat(@PathVariable("seatID") Integer seatID, HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seats");
        modelAndView.addObject("contentFragment", "admin/edit-seat");
        modelAndView.addObject("seat", seatService.getSeatById(seatID));
        modelAndView.addObject("carriageList", carriageService.findAllCarriages());
        modelAndView.addObject("seatTypeList", seatTypeService.getAllSeatTypes());
        return modelAndView;
    }

    @PostMapping("/seats/edit")
    public ModelAndView editSeat(@ModelAttribute("seat") Seat seat,
                                 @RequestParam("carriage.carriageID") Integer carriageID,
                                 @RequestParam("seatType.seatTypeID") Integer seatTypeID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "seats");
        modelAndView.addObject("contentFragment", "admin/edit-seat");

        try {
            Carriage carriage = carriageService.findCarriageById(carriageID);
            SeatType seatType = seatTypeService.getSeatTypeById(seatTypeID);
            seat.setCarriage(carriage);
            seat.setSeatType(seatType);
            seatService.updateSeat(seat.getSeatID(), seat);
            modelAndView.addObject("successMessage", "Cập nhật ghế thành công");
            modelAndView.addObject("contentFragment", "admin/seats");
            modelAndView.addObject("seatList", seatService.getAllSeats());
        } catch (Exception e) {
            modelAndView.addObject("seat", seat);
            modelAndView.addObject("errorMessage", "Lỗi khi cập nhật ghế :" + e.getMessage());
        }

        return modelAndView;
    }

    /* ========================== STATION ========================================== */

    @GetMapping("/stations")
    public ModelAndView showStationManagementPage(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "station");
        modelAndView.addObject("stationList", stationService.getAllStations());
        modelAndView.addObject("contentFragment", "admin/stations");
        return modelAndView;
    }

    @GetMapping("/stations/delete/{stationID}")
    public ModelAndView deleteStation(@PathVariable("stationID") Integer stationID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "stations");
        modelAndView.addObject("contentFragment", "admin/stations");

        try {
            stationService.deleteStation(stationID);
            modelAndView.addObject("stationList", stationService.getAllStations());
            modelAndView.addObject("successMessage", "Xóa ga tàu thành công");
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Lỗi khi xóa ga tàu: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/stations/create")
    public ModelAndView createStation(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "stations");
        modelAndView.addObject("station", new Station());
        modelAndView.addObject("contentFragment", "admin/create-station");
        return modelAndView;
    }

    @PostMapping("/stations/create")
    public ModelAndView createStation(@ModelAttribute("station") Station station, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "stations");
        modelAndView.addObject("contentFragment", "admin/create-station");

        try {
            stationService.saveStation(station);
            modelAndView.addObject("stationList", stationService.getAllStations());
            modelAndView.addObject("successMessage", "Thêm ga tàu mới thành công");
            modelAndView.addObject("contentFragment", "admin/stations");
        } catch (Exception e) {
            modelAndView.addObject("station", station);
            modelAndView.addObject("errorMessage", "Lỗi khi xảy ra khi thêm ga tàu: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/stations/edit/{stationID}")
    public ModelAndView editStation(@PathVariable("stationID") Integer stationID, HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "stations");
        modelAndView.addObject("station", stationService.getStationsByID(stationID));
        modelAndView.addObject("contentFragment", "admin/edit-station");
        return modelAndView;
    }

    @PostMapping("/stations/edit")
    public ModelAndView editStation(@ModelAttribute("station") Station station, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "stations");
        modelAndView.addObject("contentFragment", "admin/edit-station");

        try {
            stationService.updateStation(station.getStationID(), station);
            modelAndView.addObject("stationList", stationService.getAllStations());
            modelAndView.addObject("successMessage", "Cập nhật ga tàu thành công");
            modelAndView.addObject("contentFragment", "admin/stations");
        } catch (Exception e) {
            modelAndView.addObject("station", station);
            modelAndView.addObject("errorMessage", "Lỗi xảy ra khi cập nhật ga tàu: " + e.getMessage());
        }

        return modelAndView;
    }

    @GetMapping("/user")
    public ModelAndView showUser(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "user");
        modelAndView.addObject("contentFragment", "admin/user");
        modelAndView.addObject("userList", userService.getAllUsers());
        return modelAndView;
    }

    @GetMapping("/user/create")
    public ModelAndView showCreateUser(HttpSession session) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "user");
        modelAndView.addObject("contentFragment", "admin/create-user");
        modelAndView.addObject("formUser", new User());
        return modelAndView;
    }

    @PostMapping("/user/create")
    public ModelAndView createUser(@ModelAttribute("formUser") User user, HttpSession session) {
        if (!isAdmin(session)){
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "user");
        modelAndView.addObject("contentFragment", "admin/create-user");
        try {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "Người dùng thêm thành công");
            modelAndView.addObject("contentFragment", "admin/user");
            modelAndView.addObject("userList", userService.getAllUsers());
        } catch (Exception e) {
            modelAndView.addObject("user", user);
            modelAndView.addObject("errorMessage", "Lỗi khi thêm người dùng: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/user/edit/{userID}")
    public ModelAndView editUser(HttpSession session, @PathVariable("userID") Integer userID) {
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "user");
        modelAndView.addObject("contentFragment", "admin/edit-user");
        User editUser = userService.getUserById(userID);
        modelAndView.addObject("formUser", editUser);
        return modelAndView;
    }

    @PostMapping("/user/edit")
    public ModelAndView editUser(@ModelAttribute("formUser") User user, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }

        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "user");
        modelAndView.addObject("contentFragment", "admin/edit-user");
        try {
            userService.updateUser(user.getUserID(), user);
            modelAndView.addObject("successMessage", "Người dùng cập nhật thành công");
            modelAndView.addObject("contentFragment", "admin/user");
            modelAndView.addObject("userList", userService.getAllUsers());
        } catch (Exception e) {
            modelAndView.addObject("user", user);
            modelAndView.addObject("errorMessage", "Lỗi khi cập nhật người dùng: " + e.getMessage());
        }
        return modelAndView;
    }

    @GetMapping("/user/delete/{userID}")
    public ModelAndView deleteUser(@PathVariable("userID") Integer userID, HttpSession session) {
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }
        ModelAndView modelAndView = createModelAndView(session, "admin/admin-layout", "user");
        modelAndView.addObject("contentFragment", "admin/user");

        try {
            userService.deleteUserById(userID);
            modelAndView.addObject("userList", userService.getAllUsers());
            modelAndView.addObject("successMessage", "Người dùng đã được xóa thành công");
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return modelAndView;
    }

    /* ============================================================================= */
    private ModelAndView createModelAndView(HttpSession session, String viewName, String currentPage) {
        User user = (User) session.getAttribute("user");
        if (!isLoggedIn(session)) {
            return new ModelAndView("redirect:/home");
        }
        if (!isAdmin(session)) {
            return new ModelAndView("redirect:/error");
        }
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("isAdmin", true);
        modelAndView.addObject("currentPage", currentPage);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    private boolean isLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null;
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }


}

