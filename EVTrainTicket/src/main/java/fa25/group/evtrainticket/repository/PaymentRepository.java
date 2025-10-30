package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBookingBookingID(Integer bookingID);
    Optional<Payment> findByTransactionCode(String transactionCode);
    List<Payment> findByStatus(String status);
}
