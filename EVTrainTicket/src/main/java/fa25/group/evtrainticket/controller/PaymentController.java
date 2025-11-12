package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.BookingService;
import fa25.group.evtrainticket.service.TicketService;
import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Ticket;
import fa25.group.evtrainticket.entity.Payment;
import fa25.group.evtrainticket.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private PaymentRepository paymentRepository;

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

            // Create Payment entity
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalAmount());
            payment.setPaymentMethod("DEMO_PAYMENT"); // In real system, this would come from payment gateway
            payment.setPaymentDate(LocalDateTime.now());
            payment.setTransactionCode(generateTransactionCode());
            payment.setStatus("COMPLETED");
            payment.setNotes("Payment completed via demo system");
            paymentRepository.save(payment);

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
    public ResponseEntity<?> completePaymentDemo(@PathVariable String bookingCode) {
        try {
            // 1. Tìm Booking theo Code (Thay vì lấy từ Session)
            Booking booking = bookingService.getBookingByCode(bookingCode);

            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Không tìm thấy đơn hàng"));
            }

            // 2. Nếu vé đã thanh toán rồi -> Trả về thành công luôn (Tránh lỗi khi F5)
            if ("CONFIRMED".equals(booking.getStatus())) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Đã thanh toán trước đó"));
            }

            // 3. Kích hoạt vé (Chuyển từ PENDING -> ACTIVE)
            List<Ticket> tickets = ticketService.activateTicketsForBooking(bookingCode);

            // 4. Tạo lịch sử thanh toán giả (Để hiện lên trang Lịch sử vé)
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalAmount());

            // Set cứng tên phương thức để hiển thị cho đẹp
            payment.setPaymentMethod("QR_PAY_DEMO");

            payment.setPaymentDate(LocalDateTime.now());
            payment.setTransactionCode("DEMO_" + System.currentTimeMillis()); // Mã giao dịch giả
            payment.setStatus("SUCCESS");
            payment.setNotes("Thanh toán giả lập thành công");

            paymentRepository.save(payment);

            // 5. Cập nhật trạng thái đơn hàng
            booking.setStatus("CONFIRMED");
            bookingService.updateBooking(booking);

            // 6. Trả về kết quả
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bookingCode", bookingCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    /**
     * Generate unique transaction code
     */
    private String generateTransactionCode() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long count = paymentRepository.count() + 1;
        return String.format("TXN%s%04d", dateStr, count);
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
