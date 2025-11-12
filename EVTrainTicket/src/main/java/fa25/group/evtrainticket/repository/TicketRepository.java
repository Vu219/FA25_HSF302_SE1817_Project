package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByBookingBookingID(Integer bookingId);
    List<Ticket> findByBookingBookingCode(String bookingCode);
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByBooking(Booking booking);
    // --- ADD THIS METHOD ---
    @Query("SELECT t.seat.seatID FROM Ticket t WHERE t.schedule.scheduleID = :scheduleId AND t.status IN :statuses")
    List<Integer> findBookedSeatIds(@Param("scheduleId") Integer scheduleId, @Param("statuses") List<String> statuses);
    // -----------------------

    boolean existsByTicketCode(String ticketCode);

    @Query("SELECT COUNT(t) FROM Ticket t JOIN t.booking b WHERE CAST(b.bookingDate AS date) BETWEEN :startDate AND :endDate")
    long countTickets(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Ticket t")
    long countTotalTickets();

    @Query("SELECT COUNT(t) FROM Ticket t JOIN Carriage c JOIN CarriageType ct WHERE ct.typeName = 'Toa phổ thông'")
    long countEconomyTickets();



}
