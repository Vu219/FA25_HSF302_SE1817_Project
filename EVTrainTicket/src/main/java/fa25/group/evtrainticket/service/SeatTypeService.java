package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.SeatType;
import java.util.List;

public interface SeatTypeService {
    List<SeatType> getAllSeatTypes();
    SeatType getSeatTypeById(Integer id);
    SeatType saveSeatType(SeatType seatType);
    void deleteSeatType(Integer id);
    SeatType updateSeatType(Integer id, SeatType newSeatTypeData);
}