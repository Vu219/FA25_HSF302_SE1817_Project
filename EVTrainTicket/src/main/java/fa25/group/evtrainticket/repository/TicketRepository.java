package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByBookingBookingID(Integer bookingId);
    List<Ticket> findByBookingBookingCode(String bookingCode);
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByBooking(Booking booking);

    @Query("SELECT t FROM Ticket t WHERE t.booking.bookingID = ?1 AND t.status = ?2")
    List<Ticket> findByBookingIdAndStatus(Integer bookingId, String status);

    @Query("SELECT t FROM Ticket t WHERE t.booking.bookingCode = ?1 AND t.status = ?2")
    List<Ticket> findByBookingCodeAndStatus(String bookingCode, String status);

    List<Ticket> findByScheduleScheduleIDAndBookingStatusIn(Integer scheduleId, List<String> statuses);
}
