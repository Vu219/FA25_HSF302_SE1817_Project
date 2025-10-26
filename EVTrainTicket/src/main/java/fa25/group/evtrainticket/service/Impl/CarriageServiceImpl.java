package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Carriage;
import fa25.group.evtrainticket.repository.CarriageRepository;
import fa25.group.evtrainticket.service.CarriageService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarriageServiceImpl implements CarriageService {

    private final CarriageRepository carriageRepository;

    @Override
    public List<Carriage> findAllCarriages() {
        return carriageRepository.findAll();
    }

    @Override
    public Carriage findCarriageById(Integer id) {
        return carriageRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Toa tàu với ID: " + id + " không tồn tại"));
    }

    @Override
    public Carriage saveCarriage(Carriage carriage) {
        if(carriageRepository.existsByCarriageNumberAndTrain_TrainID(carriage.getCarriageNumber(), carriage.getTrain().getTrainID())) {
            throw new IllegalArgumentException("Số toa: " +  carriage.getCarriageNumber() + " đã tồn tại");
        }
        return carriageRepository.save(carriage);
    }

    @Override
    public void deleteCarriage(Integer id) {
        carriageRepository.deleteById(id);
    }

    @Override
    public Carriage updateCarriage(Integer id, Carriage newCarriage) {
        Carriage carriage1 = carriageRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Carriage not found"));
        carriage1.setCarriageNumber(newCarriage.getCarriageNumber());
        carriage1.setPosition(newCarriage.getPosition());
        carriage1.setCarriageType(newCarriage.getCarriageType());
        carriage1.setStatus(newCarriage.getStatus());
        carriage1.setTrain(newCarriage.getTrain());
        return carriageRepository.save(carriage1);
    }
}