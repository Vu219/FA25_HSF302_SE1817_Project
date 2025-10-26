package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Carriage;

import java.util.List;
import java.util.Optional;

public interface CarriageService {
    List<Carriage> findAllCarriages();
    Carriage findCarriageById(Integer id);
    Carriage saveCarriage(Carriage carriage);
    void deleteCarriage(Integer id);
    Carriage updateCarriage(Integer id, Carriage carriage);
}