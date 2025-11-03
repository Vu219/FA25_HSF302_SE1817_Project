package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    // Find all seats for a specific train
    List<Seat> findByCarriage_Train_TrainID(Integer trainId);

    // Find only available seats for a specific train
    List<Seat> findByCarriage_Train_TrainIDAndIsAvailableTrue(Integer trainId);

    // Find seats by carriage ID ordered by row and column (corrected field names)
    List<Seat> findByCarriageCarriageIDOrderByRowNumberAscColumnPositionAsc(Integer carriageId);

    // Find seat by seat number and carriage
    Seat findBySeatNumberAndCarriage_CarriageID(String seatNumber, Integer carriageId);
}
