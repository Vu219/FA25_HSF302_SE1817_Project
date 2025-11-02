package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Train;
import fa25.group.evtrainticket.repository.TrainRepository;
import fa25.group.evtrainticket.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;

    @Override
    public Train createTrain(Train train) {
        if(trainRepository.existsByTrainNumber(train.getTrainNumber())){
            throw new IllegalArgumentException("Tàu: "+ train.getTrainName() + " đã tồn tại");
        }
        return trainRepository.save(train);
    }

    @Override
    public Train getTrainById(Integer id) {
        return trainRepository.findById(id).orElseThrow(() -> new RuntimeException("Tàu với ID: " + id + " không tồn tại"));
    }

    @Override
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @Override
    public Train updateTrain(Integer id, Train trainDetails) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new RuntimeException("Train not found"));
            train.setTrainNumber(trainDetails.getTrainNumber());
            train.setTrainName(trainDetails.getTrainName());
            train.setCapacity(trainDetails.getCapacity());
            train.setStatus(trainDetails.getStatus());
            train.setNotes(trainDetails.getNotes());
        return trainRepository.save(train);
    }

    @Override
    public void deleteTrain(Integer id) {
        trainRepository.deleteById(id);
    }

    @Override
    public Train findByTrainName(String trainName) {
        return trainRepository.findByTrainName(trainName);
    }
}