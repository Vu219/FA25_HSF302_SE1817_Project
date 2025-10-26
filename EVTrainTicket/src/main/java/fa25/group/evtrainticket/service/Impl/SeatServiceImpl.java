package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Seat;
import fa25.group.evtrainticket.repository.SeatRepository;
import fa25.group.evtrainticket.service.SeatService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    @Override
    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    @Override
    public Seat getSeatById(Integer id) {
        return seatRepository.findById(id).orElseThrow(() -> new RuntimeException("Ghế tàu với ID:" + id + " không tồn tại"));
    }

    @Override
    public Seat saveSeat(Seat newSeat) {
        if(seatRepository.existsBySeatIDAndSeatNumber(newSeat.getSeatID(), newSeat.getSeatNumber())){
            throw new IllegalArgumentException("Ghế tàu với ID:" + newSeat.getSeatID() + " đã tồn tại");
        }
        return seatRepository.save(newSeat);
    }

    @Override
    public void deleteSeat(Integer id) {
        seatRepository.deleteById(id);
    }

    @Override
    public Seat updateSeat(Integer id, Seat newSeat) {
        Seat existingSeat = getSeatById(id);
        existingSeat.setSeatNumber(newSeat.getSeatNumber());
        existingSeat.setRowNum(newSeat.getRowNum());
        existingSeat.setColumnNum(newSeat.getColumnNum());
        existingSeat.setSeatType(newSeat.getSeatType());
        existingSeat.setCarriage(newSeat.getCarriage());
        return seatRepository.save(existingSeat);
    }
}