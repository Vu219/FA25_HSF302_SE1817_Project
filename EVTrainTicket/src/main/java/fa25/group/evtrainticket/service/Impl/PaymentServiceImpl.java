package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.repository.BookingRepository;
import fa25.group.evtrainticket.repository.PaymentRepository;
import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Payment;
import fa25.group.evtrainticket.service.PaymentService;
import fa25.group.evtrainticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketService ticketService;

    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_COMPLETED = "COMPLETED";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";

    public static final String BOOKING_STATUS_PENDING = "PENDING";
    public static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
    public static final String BOOKING_STATUS_CANCELLED = "CANCELLED";

    @Override
    @Transactional
    public Payment processPayment(Integer bookingId, String paymentMethod) {
        // Get booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!BOOKING_STATUS_PENDING.equals(booking.getStatus())) {
            throw new RuntimeException("Booking is not in pending status");
        }

        // Create payment with PENDING status
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "DEMO_PAYMENT");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionCode(generateTransactionCode());
        payment.setStatus(PAYMENT_STATUS_PENDING);
        payment.setNotes("Payment initiated for booking " + booking.getBookingCode());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment confirmPayment(String transactionCode) {
        // Get payment by transaction code
        Payment payment = paymentRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!PAYMENT_STATUS_PENDING.equals(payment.getStatus())) {
            throw new RuntimeException("Payment is not in pending status");
        }

        // Update payment status to COMPLETED
        payment.setStatus(PAYMENT_STATUS_COMPLETED);
        payment.setNotes("Payment confirmed successfully");
        payment = paymentRepository.save(payment);

        // Update booking status to CONFIRMED
        Booking booking = payment.getBooking();
        booking.setStatus(BOOKING_STATUS_CONFIRMED);
        bookingRepository.save(booking);

        // Activate all tickets for this booking
        ticketService.activateTickets(booking.getBookingID());

        return payment;
    }

    @Override
    public Payment getPaymentByTransactionCode(String transactionCode) {
        return paymentRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public List<Payment> getPaymentsByBookingId(Integer bookingId) {
        return paymentRepository.findByBookingBookingID(bookingId);
    }

    @Override
    @Transactional
    public Payment cancelPayment(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (PAYMENT_STATUS_COMPLETED.equals(payment.getStatus())) {
            // If payment was completed, mark as refunded
            payment.setStatus(PAYMENT_STATUS_REFUNDED);
        } else {
            // If payment was pending, mark as cancelled
            payment.setStatus(PAYMENT_STATUS_CANCELLED);
        }

        payment.setNotes("Payment cancelled/refunded");
        payment = paymentRepository.save(payment);

        // Update booking status
        Booking booking = payment.getBooking();
        booking.setStatus(BOOKING_STATUS_CANCELLED);
        bookingRepository.save(booking);

        // Cancel all tickets
        ticketService.cancelTickets(booking.getBookingID());

        return payment;
    }

    private String generateTransactionCode() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
