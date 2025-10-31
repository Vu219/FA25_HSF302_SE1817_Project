package fa25.group.evtrainticket.config;


import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.entity.Train;
import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.repository.ScheduleRepository;
import fa25.group.evtrainticket.repository.StationRepository;
import fa25.group.evtrainticket.repository.TrainRepository;
import fa25.group.evtrainticket.repository.UserRepository;
import fa25.group.evtrainticket.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInit implements CommandLineRunner {
    UserService userService;
    UserRepository userRepository;
    StationRepository stationRepository;
    ScheduleRepository scheduleRepository;
    TrainRepository trainRepository;

    @Override
    public void run(String... args) throws Exception {
        initUsers();
        initTrains();
        initStationsAndSchedules();
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            User user = new User();
            user.setFullName("Quản trị viên");
            user.setPhone("0905111111");
            user.setPassword("12345");
            user.setEmail("admin@gmail.com");
            user.setRole("ADMIN");
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            System.out.println("Đã khởi tạo dữ liệu người dùng mẫu!");
        }
    }

    private void initTrains() {
        if (trainRepository.count() == 0) {
            Train train1 = new Train();
            train1.setTrainName("Tàu SE1");
            train1.setTrainNumber("SE1");
            train1.setCapacity(300);
            train1.setStatus("Active");
            trainRepository.save(train1);

            Train train2 = new Train();
            train2.setTrainName("Tàu SE2");
            train2.setTrainNumber("SE2");
            train2.setCapacity(300);
            train2.setStatus("Active");
            trainRepository.save(train2);

            System.out.println("Đã khởi tạo dữ liệu tàu mẫu!");
        }
    }

    private void initStationsAndSchedules() {
        if (stationRepository.count() == 0) {
            // Create stations
            Station hanoi = new Station();
            hanoi.setName("Hà Nội");
            hanoi.setCode("HN");
            hanoi.setAddress("Số 1 Ga Hà Nội");
            hanoi.setCity("Hà Nội");
            hanoi.setProvince("Hà Nội");
            hanoi.setStatus("Active");
            stationRepository.save(hanoi);

            Station danang = new Station();
            danang.setName("Đà Nẵng");
            danang.setCode("DN");
            danang.setAddress("Số 200 Hải Phòng");
            danang.setCity("Đà Nẵng");
            danang.setProvince("Đà Nẵng");
            danang.setStatus("Active");
            stationRepository.save(danang);

            Station hcm = new Station();
            hcm.setName("Hồ Chí Minh");
            hcm.setCode("HCM");
            hcm.setAddress("Số 1 Nguyễn Thông");
            hcm.setCity("Hồ Chí Minh");
            hcm.setProvince("Hồ Chí Minh");
            hcm.setStatus("Active");
            stationRepository.save(hcm);

            Station nhatrang = new Station();
            nhatrang.setName("Nha Trang");
            nhatrang.setCode("NT");
            nhatrang.setAddress("Số 26 Thái Nguyên");
            nhatrang.setCity("Nha Trang");
            nhatrang.setProvince("Khánh Hòa");
            nhatrang.setStatus("Active");
            stationRepository.save(nhatrang);

            Station hue = new Station();
            hue.setName("Huế");
            hue.setCode("HUE");
            hue.setAddress("Số 2 Bùi Thị Xuân");
            hue.setCity("Huế");
            hue.setProvince("Thừa Thiên Huế");
            hue.setStatus("Active");
            stationRepository.save(hue);

            System.out.println("Đã khởi tạo dữ liệu ga mẫu!");
        }

        // ✅ TẠO SCHEDULES MỚI VỚI NGÀY TƯƠNG LAI
        if (scheduleRepository.count() == 0) {
            Train train1 = trainRepository.findByTrainName("Tàu SE1");
            Train train2 = trainRepository.findByTrainName("Tàu SE2");

            Station hanoi = stationRepository.findByCode("HN");
            Station danang = stationRepository.findByCode("DN");
            Station hcm = stationRepository.findByCode("HCM");
            Station nhatrang = stationRepository.findByCode("NT");
            Station hue = stationRepository.findByCode("HUE");

            // Lấy ngày hôm nay + thêm ngày
            LocalDateTime today = LocalDateTime.now();

            // ===== HÀ NỘI → HUẾ (Hôm nay + 1) =====
            Schedule s1 = new Schedule();
            s1.setTrain(train1);
            s1.setDepartureStation(hanoi);
            s1.setArrivalStation(hue);
            s1.setDepartureTime(today.plusDays(1).withHour(6).withMinute(0).withSecond(0));
            s1.setArrivalTime(today.plusDays(1).withHour(18).withMinute(30).withSecond(0));
            s1.setDistanceKm(688.0);
            s1.setEstimatedTime(750); // 12h30
            s1.setBasePrice(350000.0);
            s1.setStatus("ACTIVE");
            scheduleRepository.save(s1);

            // ===== HÀ NỘI → ĐÀ NẴNG (Hôm nay + 1) =====
            Schedule s2 = new Schedule();
            s2.setTrain(train2);
            s2.setDepartureStation(hanoi);
            s2.setArrivalStation(danang);
            s2.setDepartureTime(today.plusDays(1).withHour(19).withMinute(0).withSecond(0));
            s2.setArrivalTime(today.plusDays(2).withHour(10).withMinute(0).withSecond(0));
            s2.setDistanceKm(791.0);
            s2.setEstimatedTime(900); // 15h
            s2.setBasePrice(500000.0);
            s2.setStatus("ACTIVE");
            scheduleRepository.save(s2);

            // ===== ĐÀ NẴNG → NHA TRANG (Hôm nay + 2) =====
            Schedule s3 = new Schedule();
            s3.setTrain(train1);
            s3.setDepartureStation(danang);
            s3.setArrivalStation(nhatrang);
            s3.setDepartureTime(today.plusDays(2).withHour(7).withMinute(30).withSecond(0));
            s3.setArrivalTime(today.plusDays(2).withHour(17).withMinute(0).withSecond(0));
            s3.setDistanceKm(541.0);
            s3.setEstimatedTime(570); // 9h30
            s3.setBasePrice(400000.0);
            s3.setStatus("ACTIVE");
            scheduleRepository.save(s3);

            // ===== NHA TRANG → HỒ CHÍ MINH (Hôm nay + 2) =====
            Schedule s4 = new Schedule();
            s4.setTrain(train2);
            s4.setDepartureStation(nhatrang);
            s4.setArrivalStation(hcm);
            s4.setDepartureTime(today.plusDays(2).withHour(20).withMinute(0).withSecond(0));
            s4.setArrivalTime(today.plusDays(3).withHour(5).withMinute(30).withSecond(0));
            s4.setDistanceKm(411.0);
            s4.setEstimatedTime(570); // 9h30
            s4.setBasePrice(320000.0);
            s4.setStatus("ACTIVE");
            scheduleRepository.save(s4);

            // ===== HỒ CHÍ MINH → ĐÀ NẴNG (Hôm nay + 3) =====
            Schedule s5 = new Schedule();
            s5.setTrain(train1);
            s5.setDepartureStation(hcm);
            s5.setArrivalStation(danang);
            s5.setDepartureTime(today.plusDays(3).withHour(6).withMinute(0).withSecond(0));
            s5.setArrivalTime(today.plusDays(3).withHour(23).withMinute(30).withSecond(0));
            s5.setDistanceKm(935.0);
            s5.setEstimatedTime(1050); // 17h30
            s5.setBasePrice(600000.0);
            s5.setStatus("ACTIVE");
            scheduleRepository.save(s5);

            // ===== ĐÀ NẴNG → HÀ NỘI (Hôm nay + 4) =====
            Schedule s6 = new Schedule();
            s6.setTrain(train2);
            s6.setDepartureStation(danang);
            s6.setArrivalStation(hanoi);
            s6.setDepartureTime(today.plusDays(4).withHour(8).withMinute(0).withSecond(0));
            s6.setArrivalTime(today.plusDays(4).withHour(23).withMinute(0).withSecond(0));
            s6.setDistanceKm(791.0);
            s6.setEstimatedTime(900); // 15h
            s6.setBasePrice(500000.0);
            s6.setStatus("ACTIVE");
            scheduleRepository.save(s6);

            // ===== HỒ CHÍ MINH → HÀ NỘI (Hôm nay + 5) =====
            Schedule s7 = new Schedule();
            s7.setTrain(train1);
            s7.setDepartureStation(hcm);
            s7.setArrivalStation(hanoi);
            s7.setDepartureTime(today.plusDays(5).withHour(19).withMinute(30).withSecond(0));
            s7.setArrivalTime(today.plusDays(6).withHour(16).withMinute(0).withSecond(0));
            s7.setDistanceKm(1726.0);
            s7.setEstimatedTime(1230); // 20h30
            s7.setBasePrice(850000.0);
            s7.setStatus("ACTIVE");
            scheduleRepository.save(s7);

            System.out.println("✅ Đã khởi tạo " + scheduleRepository.count() + " lịch trình mẫu!");
        }
    }
}
