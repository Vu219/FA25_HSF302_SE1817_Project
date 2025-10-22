package fa25.group.evtrainticket.Service;

import fa25.group.evtrainticket.Repository.CarriageRepository;
import fa25.group.evtrainticket.Repository.ScheduleRepository;
import fa25.group.evtrainticket.Repository.SeatRepository;
import fa25.group.evtrainticket.Repository.TicketRepository;
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
public class SeatServiceImpl implements fa25.group.evtrainticket.Service.SeatService {
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
            List<Seat> seatsInCarriage = seatRepository.findByCarriageCarriageIDOrderByRowNumAscColumnNumAsc(carriage.getCarriageID());
            List<seatDto> seatDTOs = new ArrayList<>();

            for (Seat seat : seatsInCarriage) {
                SeatStatus seatStatus = occupiedSeats.contains(seat.getSeatID()) ? SeatStatus.BOOKED : SeatStatus.AVAILABLE;
                BigDecimal finalPrice = basePrice.multiply(carriage.getCarriageType().getPriceMultiplier())
                        .multiply(seat.getSeatType().getPriceMultiplier());
                seatDTOs.add(new seatDto(seat, finalPrice, seatStatus));
            }
        }
        return carriageLayouts;
    }
}
