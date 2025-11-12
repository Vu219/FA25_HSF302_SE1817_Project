package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query(
            "SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.tickets t LEFT JOIN FETCH t.seat WHERE b.bookingCode = :bookingCode"
    )
    Optional<Booking> findByBookingCode(
            @Param("bookingCode") String bookingCode
    );

    List<Booking> findByUserEmail(String email);
    List<Booking> findByUserUserIDOrderByBookingDateDesc(Integer userID);

    @Query(
            "SELECT b FROM Booking b WHERE b.user.userID = :userId " +
                    "AND (COALESCE(:status, '') = '' OR b.status = :status) " +
                    "AND (:fromDate IS NULL OR b.bookingDate >= :fromDate) " +
                    "AND (:toDate IS NULL OR b.bookingDate <= :toDate) " +
                    "ORDER BY b.bookingDate DESC"
    )
    List<Booking> findFilteredBookings(
            @Param("userId") Integer userId,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

}
