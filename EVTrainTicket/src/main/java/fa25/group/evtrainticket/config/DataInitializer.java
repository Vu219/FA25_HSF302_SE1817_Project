package fa25.group.evtrainticket.Config;

import com.yourproject.entity.*; // Import tất cả entity
import com.yourproject.repository.*; // Import tất cả repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Lớp này sẽ tự động chạy khi Spring Boot khởi động.
 * Nó dùng các Repository để chèn dữ liệu test một cách an toàn.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    // Tiêm (Inject) tất cả các repository bạn cần
    @Autowired private StationRepository stationRepository;
    @Autowired private TrainRepository trainRepository;
    @Autowired private CarriageTypeRepository carriageTypeRepository;
    @Autowired private SeatTypeRepository seatTypeRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private CarriageRepository carriageRepository;
    @Autowired private SeatRepository seatRepository;

    @Override
    @Transactional // Rất quan trọng, để đảm bảo tất cả cùng thành công
    public void run(String... args) throws Exception {

        // Chỉ chèn nếu chưa có dữ liệu
        if (stationRepository.count() == 0) {
            System.out.println("--- BẮT ĐẦU CHÈN DỮ LIỆU TEST ---");

            // 1. Ga tàu
            Station station1 = new Station();
            station1.setName("Ga Hà Nội");
            station1.setCode("HN");
            station1.setAddress("120 Le Duan");
            station1.setCity("Hà Nội");
            station1.setProvince("Hà Nội");
            station1.setStatus("active");
            stationRepository.save(station1); // Lưu và lấy đối tượng đã lưu

            Station station2 = new Station();
            station2.setName("Ga Sài Gòn");
            station2.setCode("SG");
            station2.setAddress("1 Nguyen Thong");
            station2.setCity("TP. Hồ Chí Minh");
            station2.setProvince("TP. Hồ Chí Minh");
            station2.setStatus("active");
            stationRepository.save(station2);

            // 2. Tàu
            Train train1 = new Train();
            train1.setTrainNumber("SE1");
            train1.setTrainName("Tàu SE1");
            train1.setCapacity(500);
            train1.setStatus("ready");
            trainRepository.save(train1);

            // 3. Loại toa
            CarriageType ct1 = new CarriageType();
            ct1.setTypeName("Ngồi mềm điều hòa");
            ct1.setSeatCount(64);
            ct1.setPriceMultiplier(1.2);
            carriageTypeRepository.save(ct1);

            // 4. Loại ghế
            SeatType st1 = new SeatType();
            st1.setTypeName("Ghế thường");
            st1.setPriceMultiplier(1.0);
            seatTypeRepository.save(st1);

            // 5. Lịch trình (ScheduleID = 1)
            Schedule schedule1 = new Schedule();
            schedule1.setDepartureStation(station1); // <-- Dùng đối tượng (an toàn)
            schedule1.setArrivalStation(station2);   // <-- Dùng đối tượng (an toàn)
            schedule1.setTrain(train1);              // <-- Dùng đối tượng (an toàn)
            schedule1.setDepartureTime(LocalDateTime.parse("2025-11-20T19:30:00"));
            schedule1.setArrivalTime(LocalDateTime.parse("2025-11-21T04:00:00"));
            schedule1.setDistanceKm(1726.0);
            schedule1.setEstimatedTime(2070);
            schedule1.setBasePrice(700000.0);
            schedule1.setStatus("scheduled");
            schedule1.setCreatedAt(LocalDateTime.now());
            scheduleRepository.save(schedule1);

            // 6. Toa tàu (Cho Tàu 1)
            Carriage c1 = new Carriage();
            c1.setTrain(train1);
            c1.setCarriageNumber("Toa 1");
            c1.setCarriageType(ct1);
            c1.setPosition(1);
            c1.setStatus("active");
            carriageRepository.save(c1);

            Carriage c2 = new Carriage();
            c2.setTrain(train1);
            c2.setCarriageNumber("Toa 2");
            c2.setCarriageType(ct1);
            c2.setPosition(2);
            c2.setStatus("active");
            carriageRepository.save(c2);

            // 7. Ghế (Cho 2 toa trên)
            // Ghế Toa 1
            Seat s1 = new Seat();
            s1.setCarriage(c1);
            s1.setSeatNumber("A1");
            s1.setSeatType(st1);
            s1.setPosition("Cửa sổ");
            s1.setRowNum(1);
            s1.setColumnNum(1);
            seatRepository.save(s1);

            Seat s2 = new Seat();
            s2.setCarriage(c1);
            s2.setSeatNumber("A2");
            s2.setSeatType(st1);
            s2.setPosition("Giữa");
            s2.setRowNum(1);
            s2.setColumnNum(2);
            seatRepository.save(s2);

            // Ghế Toa 2
            Seat s3 = new Seat();
            s3.setCarriage(c2);
            s3.setSeatNumber("B1");
            s3.setSeatType(st1);
            s3.setPosition("Cửa sổ");
            s3.setRowNum(1);
            s3.setColumnNum(1);
            seatRepository.save(s3);

            System.out.println("--- CHÈN DỮ LIỆU TEST THÀNH CÔNG ---");
        }
    }
}
