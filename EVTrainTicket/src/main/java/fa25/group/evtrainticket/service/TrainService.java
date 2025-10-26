package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Train;

import java.util.List;
import java.util.Optional;

public interface TrainService {
    Train createTrain(Train train);
    Train getTrainById(Integer id);
    List<Train> getAllTrains();
    Train updateTrain(Integer id, Train trainDetails);
    void deleteTrain(Integer id);
}