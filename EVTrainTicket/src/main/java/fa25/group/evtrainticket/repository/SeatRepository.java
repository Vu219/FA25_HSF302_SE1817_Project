package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat,Integer> {
    boolean existsBySeatIDAndSeatNumber(Integer seatID, String seatNumber);
}
