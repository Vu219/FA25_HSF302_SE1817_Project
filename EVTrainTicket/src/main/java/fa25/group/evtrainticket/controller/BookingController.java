package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.BookingService;
import fa25.group.evtrainticket.dto.BookingRequestDto;

import fa25.group.evtrainticket.mapper.BookingMapper;
import fa25.group.evtrainticket.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final ScheduleService scheduleService;

    // Web pages
    @GetMapping("/booking")
    public String bookingPage() {
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
            if (booking == null) {
                model.addAttribute("error", "Booking not found with code: " + bookingCode);
                return "booking/booking-confirmation";
            }

            // Add booking details to model
            model.addAttribute("booking", booking);
            model.addAttribute("bookingCode", bookingCode);
            model.addAttribute("status", status);
            model.addAttribute("totalAmount", "$" + booking.getTotalAmount().toString());

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
    public String paymentPage(HttpSession session, Model model,
                              @RequestParam(name = "bookingCode", required = false) String bookingCodeParam) {

        String bookingCode = (String) session.getAttribute("currentBookingCode");

        // Nếu không có trong session, thử lấy từ param (dành cho thanh toán lại)
        if (bookingCode == null && bookingCodeParam != null) {
            bookingCode = bookingCodeParam;
            // Đặt lại vào session để luồng thanh toán hoạt động
            session.setAttribute("currentBookingCode", bookingCode);
        }

        if (bookingCode == null) {
            // Nếu vẫn không có code, quay về trang đặt vé
            return "redirect:/booking";
        }

        try {
            var booking = bookingService.getBookingByCode(bookingCode);
            if (booking == null) {
                return "redirect:/booking?error=notfound";
            }

            // Nếu đã thanh toán, chuyển thẳng đến trang xác nhận
            if ("CONFIRMED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus())) {
                return "redirect:/booking/confirmation?bookingCode=" + booking.getBookingCode();
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
                                          @RequestParam(name = "paymentMethod", defaultValue = "DEMO_PAYMENT") String paymentMethod) {
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
        try {
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
        try {
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
        try {
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
        try {
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
        try {
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
        try {
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
        try {
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

    @GetMapping("/api/booking/check-price")
    @ResponseBody
    public ResponseEntity<?> checkPrice(@RequestBody BookingRequestDto bookingRequest) {
        try {
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

    @GetMapping("/booking/passenger-info")
    public String passengerInfoPage(@RequestParam(name = "scheduleId") Integer scheduleId,
                                    @RequestParam(name = "seatIds") List<Integer> seatIds,
                                    Model model) {
        try {
            // Validate schedule and seats
            var schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                return "redirect:/booking?error=Schedule not found";
            }

            // Add data to model
            model.addAttribute("schedule", schedule);
            model.addAttribute("seatIds", seatIds);
            model.addAttribute("selectedSeatsCount", seatIds.size());

            return "passenger-info";
        } catch (Exception e) {
            return "redirect:/booking?error=" + e.getMessage();
        }
    }
}
