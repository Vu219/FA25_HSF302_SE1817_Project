package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.repository.CarriageRepository;
import fa25.group.evtrainticket.repository.ScheduleRepository;
import fa25.group.evtrainticket.repository.SeatRepository;
import fa25.group.evtrainticket.repository.TicketRepository;
import fa25.group.evtrainticket.dto.CarriageLayoutDto;
import fa25.group.evtrainticket.dto.SeatStatus;
import fa25.group.evtrainticket.dto.seatDto;
import fa25.group.evtrainticket.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private CarriageRepository carriageRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public List<CarriageLayoutDto> getSeatLayout(int scheduleId) {
        Schedule schedules = scheduleRepository.findById(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
        Train train = schedules.getTrain();
        BigDecimal basePrice = schedules.getBasePrice();

        List<String> occupiedStatuses = List.of("pending", "confirmed");
        List<Ticket> occupiedTickets = ticketRepository.findByScheduleScheduleIDAndBookingStatusIn(scheduleId, occupiedStatuses);
        Set<Integer> occupiedSeats = occupiedTickets.stream()
                .map(ticket -> ticket.getSeat().getSeatID())
                .collect(Collectors.toSet());

        List<Carriage> carriages = carriageRepository.findByTrainTrainIDOrderByPositionAsc(train.getTrainID());
        List<CarriageLayoutDto> carriageLayouts = new ArrayList<>();

        for (Carriage carriage : carriages) {
            List<Seat> seatsInCarriage = seatRepository.findByCarriageCarriageIDOrderByRowNumberAscColumnPositionAsc(carriage.getCarriageID());
            List<seatDto> seatDTOs = new ArrayList<>();

            for (Seat seat : seatsInCarriage) {
                SeatStatus seatStatus = occupiedSeats.contains(seat.getSeatID()) ? SeatStatus.BOOKED : SeatStatus.AVAILABLE;
                BigDecimal finalPrice = basePrice.multiply(carriage.getCarriageType().getPriceMultiplier())
                        .multiply(seat.getSeatType().getPriceMultiplier());
                seatDTOs.add(new seatDto(seat, finalPrice, seatStatus));
            }

            CarriageLayoutDto carriageLayout = new CarriageLayoutDto(
                    carriage.getCarriageID(),
                    carriage.getCarriageNumber(),
                    carriage.getCarriageType().getTypeName(),
                    carriage.getTotalSeats(),
                    seatDTOs
            );
            carriageLayouts.add(carriageLayout);
        }
        return carriageLayouts;
    }

    @Override
    public List<seatDto> getSeatsByScheduleId(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if (schedule == null) {
            return List.of(); // Return empty list if schedule not found
        }

        // Get all seats for the train associated with this schedule
        List<Seat> seats = seatRepository.findByCarriage_Train_TrainID(schedule.getTrain().getTrainID());

        return seats.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<seatDto> getAvailableSeatsByScheduleId(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if (schedule == null) {
            return List.of(); // Return empty list if schedule not found
        }

        // Get available seats for the train associated with this schedule
        List<Seat> availableSeats = seatRepository.findByCarriage_Train_TrainIDAndIsAvailableTrue(schedule.getTrain().getTrainID());

        return availableSeats.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private seatDto convertToDto(Seat seat) {
        seatDto dto = new seatDto();
        dto.setSeatID(seat.getSeatID());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setIsAvailable(seat.getIsAvailable());
        dto.setRowNumber(seat.getRowNumber());
        dto.setColumnPosition(seat.getColumnPosition());
        dto.setCarriageNumber(seat.getCarriage().getCarriageNumber());
        dto.setSeatTypeName(seat.getSeatType().getTypeName());
        return dto;
    }
}
