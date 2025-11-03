package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.BookingService;
import fa25.group.evtrainticket.dto.BookingRequestDto;

import fa25.group.evtrainticket.mapper.BookingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingMapper bookingMapper;

    // Web pages
    @GetMapping("/booking")
    public String bookingPage() {
        return "booking";
    }

    @GetMapping("/booking/confirmation")
    public String bookingConfirmationPage(@RequestParam(required = false) String bookingCode,
                                          @RequestParam(required = false) String status,
                                          Model model) {
        if (bookingCode == null || bookingCode.isEmpty()) {
            // Redirect to booking page if no booking code provided
            return "redirect:/booking";
        }

        try {
            // Get booking details
            var booking = bookingService.getBookingByCode(bookingCode);
            if (booking == null) {
                model.addAttribute("error", "Booking not found with code: " + bookingCode);
                return "booking-confirmation";
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

        return "booking-confirmation";
    }

    @GetMapping("/booking/history")
    public String bookingHistoryPage() {
        return "booking-history";
    }

    @GetMapping("/payment-ticket-test")
    public String paymentTicketTestPage() {
        return "payment-ticket-test";
    }

    @GetMapping("/payment-test-simple")
    public String paymentTestSimplePage() {
        return "payment-ticket-test-simple";
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
    public ResponseEntity<?> processPayment(@RequestParam String bookingCode,
                                          @RequestParam(defaultValue = "DEMO_PAYMENT") String paymentMethod) {
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
    public ResponseEntity<?> confirmPayment(@RequestParam String bookingCode) {
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
    public ResponseEntity<?> getBooking(@PathVariable String bookingCode) {
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
    public ResponseEntity<?> getBookingsByEmail(@PathVariable String email) {
        try {
            var bookings = bookingService.getBookingsByEmail(email);
            var response = bookingMapper.toDtoList(bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch bookings: " + e.getMessage());
        }
    }

    @PostMapping("/api/booking/cancel/{bookingId}")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(@PathVariable Integer bookingId) {
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
    public ResponseEntity<?> validateSeatAvailability(@RequestParam Integer scheduleId,
                                                    @RequestParam List<Integer> seatIds) {
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
}
