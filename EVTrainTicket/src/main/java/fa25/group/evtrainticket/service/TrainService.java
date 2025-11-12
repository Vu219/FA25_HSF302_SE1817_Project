package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Train;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TrainService {
    Train createTrain(Train train);
    Train getTrainById(Integer id);
    List<Train> getAllTrains();
    Train updateTrain(Integer id, Train trainDetails);
    void deleteTrain(Integer id);
    Train findByTrainName(String trainName);
    void checkSchedules(List<Schedule> schedules, Integer trainId);
    Map<String, Object> getSeatStatsByTrain(Integer trainId);

}