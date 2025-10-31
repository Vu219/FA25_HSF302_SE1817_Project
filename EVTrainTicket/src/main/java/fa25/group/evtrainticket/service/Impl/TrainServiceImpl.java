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
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @Override
    public Train getTrainById(Integer id) {
        return trainRepository.findById(id).orElse(null);
    }

    @Override
    public Train saveTrain(Train train) {
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