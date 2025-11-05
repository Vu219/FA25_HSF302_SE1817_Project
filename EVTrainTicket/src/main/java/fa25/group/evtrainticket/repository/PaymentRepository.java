package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBookingBookingID(Integer bookingID);
    Optional<Payment> findByTransactionCode(String transactionCode);
    List<Payment> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    long getTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND CAST(p.succeededAt AS date) BETWEEN :startDate AND :endDate")
    long getRevenue(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
