package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Payment;

import java.util.List;

public interface PaymentService {
    /**
     * Process payment for a booking
     * @param bookingId The booking ID
     * @param paymentMethod The payment method (will be expanded later)
     * @return Payment object
     */
    Payment processPayment(Integer bookingId, String paymentMethod);

    /**
     * Confirm payment completion and activate tickets
     * @param transactionCode The transaction code
     * @return Updated payment object
     */
    Payment confirmPayment(String transactionCode);

    /**
     * Get payment by transaction code
     * @param transactionCode The transaction code
     * @return Payment object
     */
    Payment getPaymentByTransactionCode(String transactionCode);

    /**
     * Get payments for a booking
     * @param bookingId The booking ID
     * @return List of payments
     */
    List<Payment> getPaymentsByBookingId(Integer bookingId);

    /**
     * Cancel payment and update related entities
     * @param paymentId The payment ID
     * @return Updated payment object
     */
    Payment cancelPayment(Integer paymentId);
}
