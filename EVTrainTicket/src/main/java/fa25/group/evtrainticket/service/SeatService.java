package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Seat;
import fa25.group.evtrainticket.entity.SeatType;

import java.util.List;

public interface SeatService {
    List<Seat> getAllSeats();
    Seat getSeatById(Integer id);
    Seat saveSeat(Seat newSeat);
    void deleteSeat(Integer id);
    Seat updateSeat(Integer id, Seat newSeatData);
}
