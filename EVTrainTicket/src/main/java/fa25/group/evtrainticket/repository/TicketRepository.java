package fa25.group.evtrainticket.Repository;

import fa25.group.evtrainticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByScheduleScheduleIDAndBookingStatusIn(Integer ScheduleID, Collection<String> bookingStatuses);
}
