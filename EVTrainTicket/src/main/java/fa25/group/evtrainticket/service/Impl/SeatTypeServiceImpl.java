package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.SeatType;
import fa25.group.evtrainticket.repository.SeatTypeRepository;
import fa25.group.evtrainticket.service.SeatTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatTypeServiceImpl implements SeatTypeService {

    @Autowired
    private SeatTypeRepository seatTypeRepository;

    @Override
    public List<SeatType> getAllSeatTypes() {
        return seatTypeRepository.findAll();
    }

    @Override
    public SeatType getSeatTypeById(Integer id) {
        return seatTypeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Loại ghê với ID: " + id + " không tồn tại"));
    }

    @Override
    public SeatType saveSeatType(SeatType seatType) {
        if(seatTypeRepository.existsBySeatTypeIDAndTypeName(seatType.getSeatTypeID(), seatType.getTypeName())){
            throw new IllegalArgumentException("Loại ghế: "+ seatType.getTypeName() + " đã tồn tại");
        }
        return seatTypeRepository.save(seatType);
    }

    @Override
    public void deleteSeatType(Integer id) {
        seatTypeRepository.deleteById(id);
    }

    @Override
    public SeatType updateSeatType(Integer id, SeatType newSeatTypeData){
        SeatType oldSeatType = getSeatTypeById(id);
        oldSeatType.setTypeName(newSeatTypeData.getTypeName());
        oldSeatType.setPriceMultiplier(newSeatTypeData.getPriceMultiplier());
        oldSeatType.setDescription(newSeatTypeData.getDescription());
        return seatTypeRepository.save(oldSeatType);
    }
}