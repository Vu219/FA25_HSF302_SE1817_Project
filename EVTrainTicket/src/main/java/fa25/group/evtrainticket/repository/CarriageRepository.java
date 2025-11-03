package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Carriage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CarriageRepository extends JpaRepository<Carriage, Integer> {
    List<Carriage> findByTrainTrainIDOrderByPositionAsc(int trainId);
}
