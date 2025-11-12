package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.SeatStatus;
import fa25.group.evtrainticket.dto.seatDto;
import fa25.group.evtrainticket.entity.*;
import fa25.group.evtrainticket.repository.CarriageRepository;
import fa25.group.evtrainticket.repository.ScheduleRepository;
import fa25.group.evtrainticket.repository.SeatRepository;
import fa25.group.evtrainticket.repository.TicketRepository;
import fa25.group.evtrainticket.service.SeatService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final CarriageRepository carriageRepository;


    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public List<CarriageLayoutDto> getSeatLayout(int scheduleId) {
        // Bước A: Lấy schedule để có basePrice
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + scheduleId));

        // Bước B: Lấy tất cả ghế vật lý
        List<Seat> allSeats = seatRepository.findByCarriage_Train_Schedules_ScheduleID(scheduleId);

        if (allSeats.isEmpty()) {
            return new ArrayList<>();
        }

        // Bước C: Chuyển đổi Entity sang DTO với giá được tính toán
        Map<Integer, List<seatDto>> seatsByCarriageId = new HashMap<>();
        Map<Integer, Carriage> carriageMap = new HashMap<>();

        for (Seat seat : allSeats) {
            // TÍNH TOÁN GIÁ VÉ THỰC TẾ
            BigDecimal finalPrice = calculateSeatPrice(schedule, seat);

            seatDto dto = new seatDto();
            dto.setSeatID(seat.getSeatID());
            dto.setSeatNumber(seat.getSeatNumber());
            dto.setRowNumber(seat.getRowNumber());
            dto.setColumnNum(seat.getColumnNum());
            dto.setPrice(finalPrice); // SỬA: Dùng giá đã tính toán
            dto.setSeatTypeName(seat.getSeatType().getTypeName());
            dto.setCarriageNumber(seat.getCarriage().getCarriageNumber());

            // Kiểm tra ghế có hỏng vật lý không
            if (Boolean.TRUE.equals(seat.getIsAvailable())) {
                dto.setStatus(SeatStatus.AVAILABLE);
            } else {
                dto.setStatus(SeatStatus.BOOKED);
            }

            int carriageId = seat.getCarriage().getCarriageID();
            seatsByCarriageId.computeIfAbsent(carriageId, k -> new ArrayList<>()).add(dto);
            carriageMap.putIfAbsent(carriageId, seat.getCarriage());
        }

        List<String> bookedStatuses = Arrays.asList("PAID", "BOOKED", "PENDING", "PROCESSING", "PENDING_PAYMENT", "ACTIVE");
        List<Integer> bookedSeatIds = ticketRepository.findBookedSeatIds(scheduleId, bookedStatuses);

        List<CarriageLayoutDto> layoutResult = new ArrayList<>();

        for (Map.Entry<Integer, List<seatDto>> entry : seatsByCarriageId.entrySet()) {
            Integer carriageId = entry.getKey();
            List<seatDto> seatDtos = entry.getValue();
            Carriage carriageInfo = carriageMap.get(carriageId);

            // --- CẬP NHẬT TRẠNG THÁI TỪ DB VÉ ---
            for (seatDto sDto : seatDtos) {
                if (bookedSeatIds.contains(sDto.getSeatID())) {
                    sDto.setStatus(SeatStatus.BOOKED); // [QUAN TRỌNG] Dùng Enum
                }
            }

            CarriageLayoutDto carriageLayout = new CarriageLayoutDto();
            carriageLayout.setCarriageId(carriageId);
            carriageLayout.setCarriageNumber(carriageInfo.getCarriageNumber());
            carriageLayout.setCarriageTypeName(carriageInfo.getCarriageType().getTypeName());
            carriageLayout.setTotalSeats(seatDtos.size());
            carriageLayout.setSeats(seatDtos);

            layoutResult.add(carriageLayout);
        }


        // Sắp xếp theo chuỗi ký tự (String) -> Chấp nhận cả "C2", "Toa 1", v.v.
        layoutResult.sort(Comparator.comparing(CarriageLayoutDto::getCarriageNumber));
        return layoutResult;
    }

    private BigDecimal calculateSeatPrice(Schedule schedule, Seat seat) {
        BigDecimal basePrice = schedule.getBasePrice();
        BigDecimal seatMultiplier = seat.getSeatType().getPriceMultiplier();
        BigDecimal carriageMultiplier = seat.getCarriage().getCarriageType().getPriceMultiplier();

        return basePrice.multiply(seatMultiplier).multiply(carriageMultiplier);
    }

    private seatDto convertToDto(Seat seat) {
        seatDto dto = new seatDto();
        dto.setSeatID(seat.getSeatID());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setIsAvailable(seat.getIsAvailable());
        dto.setRowNumber(seat.getRowNumber());
        dto.setCarriageNumber(seat.getCarriage().getCarriageNumber());
        dto.setSeatTypeName(seat.getSeatType().getTypeName());
        return dto;
    }

    @Override
    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    @Override
    public Seat getSeatById(Integer id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ghế tàu với ID:" + id + " không tồn tại"));
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
        existingSeat.setRowNumber(newSeat.getRowNumber());
        existingSeat.setColumnNum(newSeat.getColumnNum());
        existingSeat.setSeatType(newSeat.getSeatType());
        existingSeat.setCarriage(newSeat.getCarriage());
        return seatRepository.save(existingSeat);
    }
}