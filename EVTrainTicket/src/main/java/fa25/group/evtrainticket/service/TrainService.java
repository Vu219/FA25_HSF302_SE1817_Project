package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Train;
import java.util.List;

public interface TrainService {
    List<Train> getAllTrains();
    Train getTrainById(Integer id);
    Train saveTrain(Train train);
    void deleteTrain(Integer id);
    Train findByTrainName(String trainName);
}