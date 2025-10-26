package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Carriage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarriageRepository extends JpaRepository<Carriage, Integer> {
    boolean existsByCarriageNumber(String carriageNumber);

    boolean existsByCarriageNumberAndCarriageID(String carriageNumber, Integer carriageID);

    boolean existsByCarriageNumberAndTrain_TrainID(String carriageNumber, Integer trainID);
}