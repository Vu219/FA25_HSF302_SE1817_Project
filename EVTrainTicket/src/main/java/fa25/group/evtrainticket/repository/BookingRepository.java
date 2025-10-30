package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByUserEmail(String email);
    List<Booking> findByUserUserIDOrderByBookingDateDesc(Integer userID);
    List<Booking> findByUserEmailOrderByBookingDateDesc(String email);
    List<Booking> findByStatus(String status);
}
