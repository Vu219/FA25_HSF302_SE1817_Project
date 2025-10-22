package fa25.group.evtrainticket.Repository;

import fa25.group.evtrainticket.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByCarriageCarriageIDOrderByRowNumAscColumnNumAsc(Integer carriageId);
}
