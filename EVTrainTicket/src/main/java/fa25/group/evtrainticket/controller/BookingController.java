package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.seatDto;
import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.service.BookingService;
import fa25.group.evtrainticket.dto.BookingRequestDto;
import fa25.group.evtrainticket.dto.BookingResponseDto; // <--- ADDED THIS IMPORT

import fa25.group.evtrainticket.mapper.BookingMapper;
import fa25.group.evtrainticket.service.ScheduleService;
import fa25.group.evtrainticket.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final ScheduleService scheduleService;
    private final SeatService seatService;

    // Web pages
    @GetMapping("/booking")
    public String bookingPage(@RequestParam(name = "scheduleId", required = false) Integer scheduleId,
                              Model model) {

        if (scheduleId == null) {
            return "booking/booking";
        }

        try {
            var schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                model.addAttribute("error", "Không tìm thấy lịch trình tàu với ID: " + scheduleId);
                return "booking/booking";
            }

            List<CarriageLayoutDto> carriageLayouts = seatService.getSeatLayout(scheduleId);

            for (CarriageLayoutDto carriage : carriageLayouts) {
                Map<Integer, List<seatDto>> seatsByRow = carriage.getSeats()
                        .stream()
                        .filter(s -> s.getRowNumber() != null)
                        .collect(Collectors.groupingBy(seatDto::getRowNumber));

                carriage.setSeatsByRow(seatsByRow);
            }

            model.addAttribute("schedule", schedule);
            model.addAttribute("carriageLayouts", carriageLayouts);
            model.addAttribute("scheduleId", scheduleId);

            int totalSeats = carriageLayouts.stream()
                    .mapToInt(c -> c.getSeats().size())
                    .sum();

            long availableSeats = carriageLayouts.stream()
                    .flatMap(c -> c.getSeats().stream())
                    .filter(s -> "AVAILABLE".equals(s.getStatus().toString()))
                    .count();


            model.addAttribute("totalSeats", totalSeats);
            model.addAttribute("availableSeats", availableSeats);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải sơ đồ ghế: " + e.getMessage());
        }

        return "booking/booking";
    }


    @GetMapping("/booking/confirmation")
    public String bookingConfirmationPage(@RequestParam(required = false) String bookingCode,
                                          @RequestParam(required = false) String status,
                                          Model model) {
        if (bookingCode == null || bookingCode.isEmpty()) {
            return "redirect:booking/booking";
        }

        try {
            var booking = bookingService.getBookingByCode(bookingCode);
            if (booking == null) { //NOSONAR
                model.addAttribute("error", "Booking not found with code: " + bookingCode);
                return "booking/booking-confirmation";
            }

            model.addAttribute("booking", booking);
            model.addAttribute("bookingCode", bookingCode);
            model.addAttribute("status", status);

            // Check strictly for COMPLETED to allow the 'Pay Now' button to show for CONFIRMED bookings
            boolean isCompleted = "COMPLETED".equals(booking.getStatus());
            model.addAttribute("paymentCompleted", isCompleted);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading booking details: " + e.getMessage());
        }

        return "booking/booking-confirmation";
    }

    @GetMapping("/booking/history")
    public String bookingHistoryPage(
            HttpSession session,
            Model model,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);

        // --- FIX: Changed from BookingRequestDto to BookingResponseDto ---
        List<BookingResponseDto> bookings;

        bookings = bookingService.getUserBookingsWithFilter(user.getUserID(), status, fromDate, toDate);

        model.addAttribute("bookings", bookings);

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedFromDate", fromDate);
        model.addAttribute("selectedToDate", toDate);

        return "booking/booking-history";
    }

    // In BookingController.java

    @GetMapping("/payment")
    public String paymentPage(
            @RequestParam(name = "bookingCode", required = false) String bookingCode, // 1. Check URL Param
            HttpSession session,
            Model model) {

        // 2. If URL param is missing, try getting it from Session (fallback for new bookings)
        if (bookingCode == null) {
            bookingCode = (String) session.getAttribute("currentBookingCode");
        }

        // 3. If both are missing, then redirect
        if (bookingCode == null) {
            return "redirect:/booking";
        }

        try {
            var booking = bookingService.getBookingByCode(bookingCode);

            // Safety check: If DB returns null
            if (booking == null) {
                return "redirect:/booking";
            }

            model.addAttribute("booking", booking);
            model.addAttribute("bookingCode", bookingCode);
            return "payment";
        } catch (Exception e) {
            return "redirect:/booking";
        }
    }

    // API endpoints
    @PostMapping("/api/booking/create")
    @ResponseBody
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDto bookingRequest) {
        try {
            var booking = bookingService.createAnonymousBooking(bookingRequest);
            var response = bookingMapper.toDto(booking);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Booking failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/booking/create-pending")
    @ResponseBody
    public ResponseEntity<?> createPendingBooking(@RequestBody BookingRequestDto bookingRequest, HttpSession session) {
        try {
            var booking = bookingService.createAnonymousPendingBooking(bookingRequest);
            var response = bookingMapper.toDto(booking);

            session.setAttribute("currentBookingCode", booking.getBookingCode());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Booking creation failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/booking/process-payment")
    @ResponseBody
    public ResponseEntity<?> processPayment(@RequestParam(name = "bookingCode") String bookingCode,
                                            @RequestParam(name = "paymentMethod", defaultValue = "CASH PAYMENT") String paymentMethod) {
        try {
            var booking = bookingService.processPayment(bookingCode, paymentMethod);
            var response = bookingMapper.toDto(booking);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "booking", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Payment processing failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/booking/confirm-payment")
    @ResponseBody
    public ResponseEntity<?> confirmPayment(@RequestParam(name = "bookingCode") String bookingCode) {
        try { //NOSONAR
            var booking = bookingService.confirmPayment(bookingCode);
            var response = bookingMapper.toDto(booking);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment confirmed successfully",
                    "booking", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Payment confirmation failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/api/booking/{bookingCode}")
    @ResponseBody
    public ResponseEntity<?> getBooking(@PathVariable(name = "bookingCode") String bookingCode) {
        try { //NOSONAR
            var booking = bookingService.getBookingByCode(bookingCode);
            var response = bookingMapper.toDto(booking);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Booking not found: " + e.getMessage());
        }
    }

    @GetMapping("/api/booking/email/{email}")
    @ResponseBody
    public ResponseEntity<?> getBookingsByEmail(@PathVariable(name = "email") String email) {
        try { //NOSONAR
            var bookings = bookingService.getBookingsByEmail(email);
            var response = bookingMapper.toDtoList(bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch bookings: " + e.getMessage());
        }
    }

    @GetMapping("/api/booking/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getBookingsByUserId(@PathVariable(name = "userId") Integer userId) {
        try { //NOSONAR
            var bookings = bookingService.getBookingsByUserId(userId);
            var response = bookingMapper.toDtoList(bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch bookings: " + e.getMessage());
        }
    }

    @PostMapping("/booking/cancel")
    public String cancelBooking(@RequestParam("bookingId") Integer bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy vé thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy vé: " + e.getMessage());
        }
        return "redirect:/booking/history";
    }

    @GetMapping("/api/booking/validate-seats")
    @ResponseBody
    public ResponseEntity<?> validateSeatAvailability(@RequestParam(name = "scheduleId") Integer scheduleId,
                                                      @RequestParam(name = "seatIds") List<Integer> seatIds) {
        try { //NOSONAR
            boolean isAvailable = bookingService.validateSeatAvailability(scheduleId, seatIds);
            return ResponseEntity.ok(Map.of(
                    "available", isAvailable
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to validate seats: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/booking/calculate-price")
    @ResponseBody
    public ResponseEntity<?> calculatePrice(@RequestBody BookingRequestDto bookingRequest) {
        try { //NOSONAR
            double price = bookingService.calculateBookingPrice(bookingRequest);
            return ResponseEntity.ok(Map.of(
                    "totalAmount", price
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to calculate price: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/booking/check-price")
    @ResponseBody
    public ResponseEntity<?> checkPrice(@RequestBody BookingRequestDto bookingRequest) {
        try { //NOSONAR
            double price = bookingService.calculateBookingPrice(bookingRequest);
            return ResponseEntity.ok(Map.of(
                    "totalAmount", price
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to calculate price: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/booking/select-seats")
    public String passengerInfoPage(@RequestParam(name = "scheduleId") Integer scheduleId,
                                    @RequestParam(name = "selectedSeats") List<Integer> seatIds,
                                    Model model) {
        try {
            var schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                return "redirect:/booking?error=Schedule not found";
            }

            var seats = scheduleService.getSeatsByScheduleId(scheduleId);

            model.addAttribute("schedule", schedule);
            model.addAttribute("seatIds", seatIds);
            model.addAttribute("selectedSeatsCount", seatIds.size());
            model.addAttribute("seats", seats);

            return "passenger-info";
        } catch (Exception e) {
            return "redirect:/booking?error=" + e.getMessage();
        }
    }
}