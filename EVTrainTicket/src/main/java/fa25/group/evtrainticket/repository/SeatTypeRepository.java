package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, Integer> {
}
