package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.seatDto;
import fa25.group.evtrainticket.service.BookingService;
import fa25.group.evtrainticket.dto.BookingRequestDto;

import fa25.group.evtrainticket.mapper.BookingMapper;
import fa25.group.evtrainticket.service.ScheduleService;
import fa25.group.evtrainticket.service.SeatService; // <-- ĐÃ THÊM IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // <-- ĐÃ THÊM IMPORT

@Controller
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final ScheduleService scheduleService;
    private final SeatService seatService; // <-- ĐÃ THÊM INJECT

    // Web pages
    @GetMapping("/booking")
    public String bookingPage(@RequestParam(name = "scheduleId", required = false) Integer scheduleId,
                              Model model) {

        // 1. Nếu không có scheduleId, chỉ hiển thị trang (HTML sẽ tự báo "Vui lòng chọn chuyến")
        if (scheduleId == null) {
            return "booking/booking";
        }

        // 2. Nếu CÓ scheduleId, tải tất cả dữ liệu (Logic được chuyển từ SeatLayoutController)
        try {
            // Get schedule information
            var schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                model.addAttribute("error", "Không tìm thấy lịch trình tàu với ID: " + scheduleId);
                return "booking/booking";
            }

            // Get seat layout data (Quan trọng)
            List<CarriageLayoutDto> carriageLayouts = seatService.getSeatLayout(scheduleId);

            // Prepare seat layout data for display (Nhóm ghế theo hàng)
            for (CarriageLayoutDto carriage : carriageLayouts) {
                // Group seats by row for better layout
                Map<Integer, List<seatDto>> seatsByRow = carriage.getSeats()
                        .stream()
                        .filter(s -> s.getRowNumber() != null) // Lọc các ghế có rowNumber
                        .collect(Collectors.groupingBy(seatDto::getRowNumber));

                carriage.setSeatsByRow(seatsByRow);
                // Bạn có thể tính maxColumns nếu cần, nhưng HTML gốc không dùng
            }

            // 3. Gửi tất cả dữ liệu sang file HTML
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

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải sơ đồ ghế: " + e.getMessage());
        }

        // 4. Trả về file HTML
        return "booking/booking";
    }


    @GetMapping("/booking/confirmation")
    public String bookingConfirmationPage(@RequestParam(required = false) String bookingCode,
                                          @RequestParam(required = false) String status,
                                          Model model) {
        if (bookingCode == null || bookingCode.isEmpty()) {
            // Redirect to booking page if no booking code provided
            return "redirect:booking/booking";
        }

        try {
            // Get booking details
            var booking = bookingService.getBookingByCode(bookingCode);
            if (booking == null) { //NOSONAR
                model.addAttribute("error", "Booking not found with code: " + bookingCode);
                return "booking/booking-confirmation";
            }

            // Add booking details to model
            model.addAttribute("booking", booking);
            model.addAttribute("bookingCode", bookingCode);
            model.addAttribute("status", status);

            // Check if payment is already completed
            boolean isCompleted = "CONFIRMED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus());
            model.addAttribute("paymentCompleted", isCompleted);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading booking details: " + e.getMessage());
        }

        return "booking/booking-confirmation";
    }

    @GetMapping("/booking/history")
    public String bookingHistoryPage(HttpSession session, Model model) {
        var user = session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "booking/booking-history";
    }

    @GetMapping("/payment")
    public String paymentPage(HttpSession session, Model model) {
        String bookingCode = (String) session.getAttribute("currentBookingCode");
        if (bookingCode == null) {
            return "redirect:/booking"; // Redirect to booking if no booking in session
        }

        try {
            var booking = bookingService.getBookingByCode(bookingCode);
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

            // Store booking code in session for payment flow
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

    @PostMapping("/api/booking/cancel/{bookingId}")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(@PathVariable(name = "bookingId") Integer bookingId) {
        try { //NOSONAR
            var booking = bookingService.cancelBooking(bookingId);
            var response = bookingMapper.toDto(booking);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Booking cancelled successfully",
                    "booking", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Booking cancellation failed: " + e.getMessage()
            ));
        }
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

    // --- ĐÂY LÀ PHẦN ĐÃ SỬA ---
    // 1. Đã đổi thành @PostMapping
    // 2. Đã đổi đường dẫn thành "/booking/select-seats" (khớp với form)
    @PostMapping("/booking/select-seats")
    public String passengerInfoPage(@RequestParam(name = "scheduleId") Integer scheduleId,
                                    // 3. Đã đổi tên param thành "selectedSeats" (khớp với form)
                                    @RequestParam(name = "selectedSeats") List<Integer> seatIds,
                                    Model model) {
        try {
            // Validate schedule and seats
            var schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                return "redirect:/booking?error=Schedule not found";
            }

            // Get all seats for the schedule
            var seats = scheduleService.getSeatsByScheduleId(scheduleId);

            // Add data to model
            model.addAttribute("schedule", schedule);
            model.addAttribute("seatIds", seatIds); // Giữ nguyên tên "seatIds" cho trang passenger-info
            model.addAttribute("selectedSeatsCount", seatIds.size());
            model.addAttribute("seats", seats); // Added seats to the model

            return "passenger-info"; // Chuyển hướng đến trang "passenger-info.html"
        } catch (Exception e) {
            return "redirect:/booking?error=" + e.getMessage();
        }
    }
}