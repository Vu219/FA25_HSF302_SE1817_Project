package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainRepository extends JpaRepository<Train, Integer> {
    Train findByTrainName(String trainName);
}