package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.CarriageType;
import fa25.group.evtrainticket.repository.CarriageTypeRepository;
import fa25.group.evtrainticket.service.CarriageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarriageTypeServiceImpl implements CarriageTypeService {

    private final CarriageTypeRepository carriageTypeRepository;

    @Override
    public List<CarriageType> findAllCarriageTypes() {
        return carriageTypeRepository.findAll();
    }

    @Override
    public CarriageType findCarriageTypeById(Integer id) {
        return carriageTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("Loại toa với ID:" + id + " này không tồn tại"));
    }

    @Override
    public CarriageType createCarriageType(CarriageType carriageType) {
        if (carriageTypeRepository.existsByTypeNameAndCarriageTypeId(carriageType.getTypeName(), carriageType.getCarriageTypeId())) {
            throw new IllegalArgumentException("Loại toa: " + carriageType.getTypeName() + " đã tồn tại");
        }
        return carriageTypeRepository.save(carriageType);
    }

    @Override
    public CarriageType updateCarriageType(Integer id, CarriageType carriageTypeDetails) {
        CarriageType carriageType = carriageTypeRepository.findById(id).orElse(null);
        if (carriageType != null) {
            carriageType.setTypeName(carriageTypeDetails.getTypeName());
            carriageType.setSeatCount(carriageTypeDetails.getSeatCount());
            carriageType.setDescription(carriageTypeDetails.getDescription());
            carriageType.setPriceMultiplier(carriageTypeDetails.getPriceMultiplier());
            return carriageTypeRepository.save(carriageType);
        }
        return null;
    }

    @Override
    public void deleteCarriageType(Integer id) {
        carriageTypeRepository.deleteById(id);
    }
}