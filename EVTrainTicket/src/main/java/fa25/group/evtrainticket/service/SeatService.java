package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.seatDto;
import fa25.group.evtrainticket.entity.Seat;

import java.util.List;

public interface SeatService {
    List<CarriageLayoutDto> getSeatLayout(int scheduleId);
    List<Seat> getAllSeats();
    Seat getSeatById(Integer id);
    Seat saveSeat(Seat newSeat);
    void deleteSeat(Integer id);
    Seat updateSeat(Integer id, Seat newSeatData);
}
