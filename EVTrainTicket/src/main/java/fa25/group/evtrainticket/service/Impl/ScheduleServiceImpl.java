package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.dto.SeatStatus;
import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Seat;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.repository.ScheduleRepository;
import fa25.group.evtrainticket.repository.SeatRepository;
import fa25.group.evtrainticket.repository.TicketRepository;
import fa25.group.evtrainticket.service.ScheduleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    // Inject EntityManager để xử lý việc Detach
    @PersistenceContext
    private final EntityManager entityManager;

    private static final String STATUS_SCHEDULED = "Active";

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public Schedule getScheduleById(Integer id) {
        return scheduleRepository.findById(id).orElse(null);
    }

    @Override
    public Schedule saveSchedule(Schedule schedule) {
        if (schedule.getDepartureStation() == null || schedule.getArrivalStation() == null) {
            throw new IllegalArgumentException("Departure and Arrival stations cannot be null.");
        }
        if (schedule.getDepartureTime() == null || schedule.getArrivalTime() == null) {
            throw new IllegalArgumentException("Departure and Arrival times cannot be null.");
        }
        if (schedule.getDepartureTime().isAfter(schedule.getArrivalTime())) {
            throw new IllegalArgumentException("Departure time cannot be after arrival time.");
        }
        return scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> saveAll(List<Schedule> schedules) {
        return scheduleRepository.saveAll(schedules);
    }

    @Override
    public void deleteSchedule(Integer id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public List<Schedule> findSchedulesByStationsAndDate(Station departureStation, Station arrivalStation, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return scheduleRepository.findByDepartureStationAndArrivalStationAndDepartureTimeBetween(departureStation, arrivalStation, startOfDay, endOfDay);
    }

    // --- PHẦN QUAN TRỌNG NHẤT ĐÃ ĐƯỢC SỬA ---
    @Override
    @Transactional(readOnly = true) // Chỉ đọc, giúp tối ưu hiệu năng
    public List<Seat> getSeatsByScheduleId(Integer scheduleId) {
        // 1. Lấy danh sách ghế gốc từ DB
        List<Seat> allSeats = seatRepository.findByCarriage_Train_Schedules_ScheduleID(scheduleId);

        // 2. Lấy danh sách ID ghế đã bán (Vé đã đặt)
        List<String> bookedStatuses = Arrays.asList("PAID", "BOOKED", "PENDING", "PROCESSING", "PENDING_PAYMENT");
        List<Integer> bookedSeatIds = ticketRepository.findBookedSeatIds(scheduleId, bookedStatuses);

        // 3. Xử lý logic hiển thị
        for (Seat seat : allSeats) {
            // QUAN TRỌNG: Ngắt kết nối object này với Database
            // Để khi mình sửa status, Hibernate KHÔNG tự động lưu vào DB
            entityManager.detach(seat);

            if (bookedSeatIds.contains(seat.getSeatID())) {
                // Ghế này có vé -> Set màu đỏ (BOOKED)
                seat.setStatus(SeatStatus.BOOKED);
            } else {
                // Ghế chưa ai đặt
                // Kiểm tra nếu ghế hỏng vật lý (dựa trên cột IsAvailable trong DB)
                if (Boolean.TRUE.equals(seat.getIsAvailable())) {
                    seat.setStatus(SeatStatus.AVAILABLE); // Màu trắng/xanh
                } else {
                    // Ghế bảo trì -> Có thể hiển thị là BOOKED hoặc màu xám khác
                    seat.setStatus(SeatStatus.BOOKED);
                }
            }
        }

        return allSeats;
    }
}