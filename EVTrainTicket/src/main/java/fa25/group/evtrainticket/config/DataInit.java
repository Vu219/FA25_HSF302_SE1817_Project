package fa25.group.evtrainticket.config;

import fa25.group.evtrainticket.entity.*;
import fa25.group.evtrainticket.repository.*;
import fa25.group.evtrainticket.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInit implements CommandLineRunner {
    UserService userService;
    UserRepository userRepository;
    TrainService trainService;
    CarriageTypeService carriageTypeService;
    CarriageService carriageService;
    OTPRepository otpRepository;
    StationRepository stationRepository;
    ScheduleRepository scheduleRepository;
    TrainRepository trainRepository;

    @Override
    public void run(String... args) throws Exception {
        initUsers();
        initTrains();
        initCarriageTypes();
        initCarriages();
        initStationsAndSchedules();
        cleanupOTPs();
    }

    private void cleanupOTPs() {
        long count = otpRepository.count();
        if (count > 0) {
            otpRepository.deleteAll();
            System.out.println("Đã xóa " + count + " OTP cũ khỏi cơ sở dữ liệu!");
        } else {
            System.out.println("Không có OTP nào trong CSDL để xóa.");
        }
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            User user = new User();
            user.setFullName("Quản trị viên");
            user.setPhone("0905111111");
            user.setPassword("123456");
            user.setEmail("admin@gmail.com");
            user.setRole("ADMIN");
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            User user1 = new User();
            user1.setFullName("Người dùng mẫu");
            user1.setPhone("0905000000");
            user1.setPassword("123456");
            user1.setEmail("user@gmail.com");
            user1.setRole("USER");
            user1.setCreatedAt(LocalDateTime.now());
            userRepository.save(user1);

            System.out.println("Đã khởi tạo dữ liệu người dùng mẫu!");
        }
    }

    private void initTrains() {
        if (trainService.getAllTrains().isEmpty()) {
            // Tạo các tàu từ phần code thứ hai (đầy đủ hơn)
            Train train1 = new Train();
            train1.setTrainNumber("TN001");
            train1.setTrainName("Tàu Thống Nhất 1");
            train1.setCapacity(600);
            train1.setStatus("Hoạt động");
            train1.setNotes("Chuyến tàu Bắc-Nam");
            trainService.createTrain(train1);

            Train train2 = new Train();
            train2.setTrainNumber("TN002");
            train2.setTrainName("Tàu Thống Nhất 2");
            train2.setCapacity(550);
            train2.setStatus("Hoạt động");
            train2.setNotes("Chuyến tàu Bắc-Nam");
            trainService.createTrain(train2);

            Train train3 = new Train();
            train3.setTrainNumber("SE003");
            train3.setTrainName("Tàu Sài Gòn-Hà Nội");
            train3.setCapacity(700);
            train3.setStatus("Hoạt động");
            train3.setNotes("Chuyến tàu nhanh");
            trainService.createTrain(train3);

            Train train4 = new Train();
            train4.setTrainNumber("DN004");
            train4.setTrainName("Tàu Đà Nẵng-Nha Trang");
            train4.setCapacity(400);
            train4.setStatus("Bảo trì");
            train4.setNotes("Tàu đang bảo dưỡng định kỳ");
            trainService.createTrain(train4);

            Train train5 = new Train();
            train5.setTrainNumber("HP005");
            train5.setTrainName("Tàu Hải Phòng-Quảng Ninh");
            train5.setCapacity(300);
            train5.setStatus("Hoạt động");
            train5.setNotes("Chuyến tàu địa phương");
            trainService.createTrain(train5);

            System.out.println("Đã khởi tạo dữ liệu tàu mẫu!");
        }
    }

    private void initCarriageTypes() {
        if (carriageTypeService.findAllCarriageTypes().isEmpty()) {
            CarriageType type1 = new CarriageType();
            type1.setTypeName("Toa phổ thông");
            type1.setSeatCount(60);
            type1.setDescription("Ghế ngồi cứng, không điều hòa");
            type1.setPriceMultiplier(1.0);
            carriageTypeService.createCarriageType(type1);

            CarriageType type2 = new CarriageType();
            type2.setTypeName("Toa cao cấp");
            type2.setSeatCount(48);
            type2.setDescription("Ghế ngồi mềm, có điều hòa");
            type2.setPriceMultiplier(1.2);
            carriageTypeService.createCarriageType(type2);

            CarriageType type3 = new CarriageType();
            type3.setTypeName("Toa hạng nhất");
            type3.setSeatCount(24);
            type3.setDescription("Giường nằm có điều hòa");
            type3.setPriceMultiplier(1.8);
            carriageTypeService.createCarriageType(type3);

            System.out.println("Đã khởi tạo dữ liệu loại toa mẫu!");
        }
    }

    private void initCarriages() {
        if (carriageService.findAllCarriages().isEmpty()) {
            // Lấy các Train và CarriageType đã được tạo
            Train train1 = trainService.getAllTrains().stream().filter(t -> t.getTrainNumber().equals("TN001")).findFirst().orElse(null);
            CarriageType type1 = carriageTypeService.findAllCarriageTypes().stream().filter(ct -> ct.getTypeName().equals("Toa phổ thông")).findFirst().orElse(null);
            CarriageType type2 = carriageTypeService.findAllCarriageTypes().stream().filter(ct -> ct.getTypeName().equals("Toa cao cấp")).findFirst().orElse(null);

            if (train1 != null && type1 != null && type2 != null) {
                Carriage carriage1 = new Carriage();
                carriage1.setTrain(train1);
                carriage1.setCarriageNumber("A01");
                carriage1.setCarriageType(type1);
                carriage1.setPosition(1);
                carriage1.setStatus("Hoạt động");
                carriageService.saveCarriage(carriage1);

                Carriage carriage2 = new Carriage();
                carriage2.setTrain(train1);
                carriage2.setCarriageNumber("A02");
                carriage2.setCarriageType(type2);
                carriage2.setPosition(2);
                carriage2.setStatus("Hoạt động");
                carriageService.saveCarriage(carriage2);

                System.out.println("Đã khởi tạo dữ liệu toa tàu mẫu!");
            } else {
                System.out.println("Không tìm thấy dữ liệu Train hoặc CarriageType để tạo Carriage mẫu.");
            }
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
            // Sử dụng các tàu đã được tạo trong initTrains()
            Train train1 = trainRepository.findByTrainNumber("TN001");
            Train train2 = trainRepository.findByTrainNumber("TN002");
            Train train3 = trainRepository.findByTrainNumber("SE003");

            // Kiểm tra nếu không tìm thấy tàu
            if (train1 == null || train2 == null || train3 == null) {
                System.err.println("❌ LỖI: Không tìm thấy tàu cần thiết để tạo lịch trình");
                List<Train> allTrains = trainRepository.findAll();
                System.err.println("Các tàu có trong database:");
                allTrains.forEach(t ->
                        System.err.println(" - " + t.getTrainNumber() + ": " + t.getTrainName()));
                return;
            }

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
            s4.setTrain(train3);
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
            s5.setTrain(train3);
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