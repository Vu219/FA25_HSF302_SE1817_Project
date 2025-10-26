package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.CarriageType;

import java.util.List;
import java.util.Optional;

public interface CarriageTypeService {
    List<CarriageType> findAllCarriageTypes();
    CarriageType findCarriageTypeById(Integer id);
    CarriageType createCarriageType(CarriageType carriageType);
    CarriageType updateCarriageType(Integer id, CarriageType carriageTypeDetails);
    void deleteCarriageType(Integer id);
}