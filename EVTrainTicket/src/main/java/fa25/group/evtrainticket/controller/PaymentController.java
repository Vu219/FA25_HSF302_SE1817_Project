package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.BookingService;
import fa25.group.evtrainticket.service.TicketService;
import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private BookingService bookingService;

    /**
     * Complete payment using booking from session (simplified for real payment flow)
     */
    @PostMapping("/complete-session")
    public ResponseEntity<?> completePaymentFromSession(HttpSession session) {
        try {
            String bookingCode = (String) session.getAttribute("currentBookingCode");

            if (bookingCode == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "No booking found in session. Please create a booking first."
                ));
            }

            Booking booking = bookingService.getBookingByCode(bookingCode);

            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Booking not found with code: " + bookingCode
                ));
            }

            if (!"PENDING".equals(booking.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Booking is not in PENDING status. Current status: " + booking.getStatus()
                ));
            }

            // Activate all tickets for this booking
            List<Ticket> tickets = ticketService.activateTicketsForBooking(bookingCode);

            // Update booking status
            booking.setStatus("CONFIRMED");
            bookingService.updateBooking(booking);

            // Clear booking from session as payment is complete
            session.removeAttribute("currentBookingCode");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment completed successfully! Your tickets are now active.");
            response.put("bookingCode", bookingCode);
            response.put("ticketCount", tickets.size());
            response.put("bookingStatus", "CONFIRMED");
            response.put("totalAmount", booking.getTotalAmount());

            // Include ticket information in response
            List<Map<String, Object>> ticketInfo = tickets.stream()
                .map(ticket -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("ticketCode", ticket.getTicketCode());
                    info.put("seatNumber", ticket.getSeat().getSeatNumber());
                    info.put("status", ticket.getStatus());
                    info.put("price", ticket.getPrice());
                    return info;
                })
                .toList();
            response.put("tickets", ticketInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to complete payment: " + e.getMessage()
            ));
        }
    }

    /**
     * Complete payment and activate tickets (simplified for testing)
     * When payment is completed by external merchant, call this endpoint
     */
    @PostMapping("/complete/{bookingCode}")
    public ResponseEntity<?> completePayment(@PathVariable String bookingCode) {
        try {
            // Simulate payment completion - in real system this would be called by payment merchant
            Booking booking = bookingService.getBookingByCode(bookingCode);

            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Booking not found with code: " + bookingCode
                ));
            }

            if (!"PENDING".equals(booking.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Booking is not in PENDING status. Current status: " + booking.getStatus()
                ));
            }

            // Activate all tickets for this booking
            List<Ticket> tickets = ticketService.activateTicketsForBooking(bookingCode);

            // Update booking status
            booking.setStatus("CONFIRMED");
            bookingService.updateBooking(booking);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment completed and tickets activated successfully");
            response.put("bookingCode", bookingCode);
            response.put("ticketCount", tickets.size());
            response.put("bookingStatus", "CONFIRMED");

            // Include ticket codes in response
            List<String> ticketCodes = tickets.stream()
                .map(Ticket::getTicketCode)
                .toList();
            response.put("ticketCodes", ticketCodes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to complete payment: " + e.getMessage()
            ));
        }
    }

    /**
     * Check payment status (for testing)
     */
    @GetMapping("/status/{bookingCode}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String bookingCode) {
        try {
            Booking booking = bookingService.getBookingByCode(bookingCode);

            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Booking not found with code: " + bookingCode
                ));
            }

            List<Ticket> tickets = ticketService.getTicketsByBookingId(booking.getBookingID());

            boolean allTicketsActive = tickets.stream()
                .allMatch(ticket -> "ACTIVE".equals(ticket.getStatus()));

            Map<String, Object> response = new HashMap<>();
            response.put("bookingCode", bookingCode);
            response.put("bookingStatus", booking.getStatus());
            response.put("totalAmount", booking.getTotalAmount());
            response.put("ticketCount", tickets.size());
            response.put("allTicketsActive", allTicketsActive);
            response.put("paymentCompleted", "CONFIRMED".equals(booking.getStatus()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get payment status: " + e.getMessage()
            ));
        }
    }
}
