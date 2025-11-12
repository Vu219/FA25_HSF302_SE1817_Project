package fa25.group.evtrainticket.config;

import fa25.group.evtrainticket.entity.*;
import fa25.group.evtrainticket.repository.*;
import fa25.group.evtrainticket.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInit implements CommandLineRunner {

    // Repositories
    UserRepository userRepository;
    TrainRepository trainRepository;
    CarriageTypeRepository carriageTypeRepository;
    CarriageRepository carriageRepository;
    SeatTypeRepository seatTypeRepository;
    SeatRepository seatRepository;
    StationRepository stationRepository;
    ScheduleRepository scheduleRepository;
    ScheduleStopRepository scheduleStopRepository;
    RouteRepository routeRepository;

    // Services
    RouteGeneratorService routeGeneratorService;

    // Services
    UserService userService;
    TrainService trainService;
    CarriageTypeService carriageTypeService;
    CarriageService carriageService;
    SeatTypeService seatTypeService;
    SeatService seatService;
    StationService stationService;
    ScheduleService scheduleService;
    ScheduleStopService scheduleStopService;

    // OTP
    OTPRepository otpRepository;

    @Override
    public void run(String... args) throws Exception {
        cleanupOTPs();
        initUsers();
        initSeatTypes();
        initCarriageTypes();
        initTrains();
        initCarriages();
        initSeats();
        initStations();
        initRoutes();

        System.out.println("\n🔄 Generating composite routes...");
        int generatedCount = routeGeneratorService.generateAllPossibleRoutes();
        System.out.println("✅ Generated " + generatedCount + " composite routes. Total routes now: " + routeRepository.count());

        initSchedules();
        initScheduleStops();
        initScheduleStops();
        System.out.println("✅ Data initialization completed successfully!");
    }

    private void cleanupOTPs() {
        long count = otpRepository.count();
        if (count > 0) {
            otpRepository.deleteAll();
            System.out.println("🗑️ Đã xóa " + count + " OTP cũ");
        }
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .fullName("Quản trị viên")
                    .email("admin@gmail.com")
                    .password("123456")
                    .phone("0905111111")
                    .role("ADMIN")
                    .createdAt(LocalDateTime.now())
                    .status("ACTIVE")
                    .build();

            User user = User.builder()
                    .fullName("Người dùng mẫu")
                    .email("user@gmail.com")
                    .password("123456")
                    .phone("0905000000")
                    .role("USER")
                    .createdAt(LocalDateTime.now())
                    .status("ACTIVE")
                    .build();

            userRepository.saveAll(Arrays.asList(admin, user));
            System.out.println("✅ Đã khởi tạo 2 người dùng mẫu");
        }
    }

    private void initSeatTypes() {
        if (seatTypeRepository.count() == 0) {
            SeatType hardSeat = new SeatType();
            hardSeat.setTypeName("Ghế cứng");
            hardSeat.setPriceMultiplier(BigDecimal.valueOf(1.0));
            hardSeat.setDescription("Ghế ngồi cứng tiêu chuẩn");

            SeatType softSeat = new SeatType();
            softSeat.setTypeName("Ghế mềm");
            softSeat.setPriceMultiplier(BigDecimal.valueOf(1.3));
            softSeat.setDescription("Ghế ngồi mềm thoải mái");

            SeatType bed = new SeatType();
            bed.setTypeName("Giường nằm");
            bed.setPriceMultiplier(BigDecimal.valueOf(2.0));
            bed.setDescription("Giường nằm có điều hòa");

            seatTypeRepository.saveAll(Arrays.asList(hardSeat, softSeat, bed));
            System.out.println("✅ Đã khởi tạo 3 loại ghế");
        }
    }

    private void initCarriageTypes() {
        if (carriageTypeRepository.count() == 0) {
            CarriageType standard = new CarriageType();
            standard.setTypeName("Toa phổ thông");
            standard.setSeatCount(60);
            standard.setDescription("Toa ghế cứng, không điều hòa");
            standard.setPriceMultiplier(BigDecimal.valueOf(1.0));

            CarriageType premium = new CarriageType();
            premium.setTypeName("Toa cao cấp");
            premium.setSeatCount(48);
            premium.setDescription("Toa ghế mềm, có điều hòa");
            premium.setPriceMultiplier(BigDecimal.valueOf(1.5));

            CarriageType sleeper = new CarriageType();
            sleeper.setTypeName("Toa giường nằm");
            sleeper.setSeatCount(24);
            sleeper.setDescription("Toa giường nằm có điều hòa");
            sleeper.setPriceMultiplier(BigDecimal.valueOf(2.2));

            carriageTypeRepository.saveAll(Arrays.asList(standard, premium, sleeper));
            System.out.println("✅ Đã khởi tạo 3 loại toa");
        }
    }

    private void initTrains() {
        if (trainRepository.count() == 0) {
            Train train1 = new Train();
            train1.setTrainNumber("TN001");
            train1.setTrainName("Tàu Thống Nhất SE1");
            train1.setCapacity(600);
            train1.setStatus("Hoạt động");
            train1.setNotes("Chuyến tàu Bắc-Nam chất lượng cao");

            Train train2 = new Train();
            train2.setTrainNumber("TN002");
            train2.setTrainName("Tàu Thống Nhất SE3");
            train2.setCapacity(550);
            train2.setStatus("Hoạt động");
            train2.setNotes("Chuyến tàu Bắc-Nam");

            Train train3 = new Train();
            train3.setTrainNumber("TN003");
            train3.setTrainName("Tàu Sài Gòn - Hà Nội");
            train3.setCapacity(700);
            train3.setStatus("Hoạt động");
            train3.setNotes("Chuyến tàu nhanh");

            trainRepository.saveAll(Arrays.asList(train1, train2, train3));
            System.out.println("✅ Đã khởi tạo 3 tàu mẫu");
        }
    }

    private void initCarriages() {
        if (carriageRepository.count() == 0) {
            List<Train> trains = trainRepository.findAll();
            List<CarriageType> carriageTypes = carriageTypeRepository.findAll();

            if (!trains.isEmpty() && !carriageTypes.isEmpty()) {
                int carriageNumber = 1;
                for (Train train : trains) {
                    for (int i = 1; i <= 3; i++) {
                        Carriage carriage = new Carriage();
                        carriage.setTrain(train);
                        carriage.setCarriageType(carriageTypes.get((i - 1) % carriageTypes.size()));
                        carriage.setCarriageNumber("C" + carriageNumber);
                        carriage.setPosition(i);
                        carriage.setStatus("Hoạt động");
                        carriage.setTotalSeats(carriage.getCarriageType().getSeatCount());

                        carriageRepository.save(carriage);
                        carriageNumber++;
                    }
                }
                System.out.println("✅ Đã khởi tạo toa tàu cho các tàu");
            }
        }
    }

    private void initSeats() {
        if (seatRepository.count() == 0) {
            List<Carriage> carriages = carriageRepository.findAll();

            // Lấy các loại ghế ra để sử dụng
            List<SeatType> allSeatTypes = seatTypeRepository.findAll();
            SeatType hardSeat = allSeatTypes.stream().filter(st -> st.getTypeName().equals("Ghế cứng")).findFirst().orElse(null);
            SeatType softSeat = allSeatTypes.stream().filter(st -> st.getTypeName().equals("Ghế mềm")).findFirst().orElse(null);
            SeatType bed = allSeatTypes.stream().filter(st -> st.getTypeName().equals("Giường nằm")).findFirst().orElse(null);

            if (hardSeat == null || softSeat == null || bed == null) {
                System.out.println("❌ Lỗi: Không tìm thấy đủ các loại ghế (Ghế cứng, Ghế mềm, Giường nằm).");
                return;
            }

            for (Carriage carriage : carriages) {
                SeatType seatTypeForThisCarriage;
                String carriageTypeName = carriage.getCarriageType().getTypeName();

                // 1. CHỌN ĐÚNG LOẠI GHẾ
                if (carriageTypeName.contains("phổ thông") || carriageTypeName.contains("cứng")) {
                    seatTypeForThisCarriage = hardSeat;
                } else if (carriageTypeName.contains("cao cấp") || carriageTypeName.contains("mềm")) {
                    seatTypeForThisCarriage = softSeat;
                } else if (carriageTypeName.contains("giường nằm")) {
                    seatTypeForThisCarriage = bed;
                } else {
                    seatTypeForThisCarriage = hardSeat; // Mặc định là ghế cứng
                }

                int seatsPerCarriage = carriage.getTotalSeats();
                // 2. SỬA LẠI LOGIC CỘT (dùng 4 cột để khớp với CSS)
                int cols = 4;
                int rows = (int) Math.ceil((double) seatsPerCarriage / cols);

                for (int row = 1; row <= rows; row++) {
                    for (int col = 1; col <= cols; col++) {
                        int seatIndex = (row - 1) * cols + col;
                        if (seatIndex > seatsPerCarriage) break;

                        Seat seat = new Seat();
                        seat.setCarriage(carriage);

                        // 3. GÁN ĐÚNG LOẠI GHẾ
                        seat.setSeatType(seatTypeForThisCarriage);

                        seat.setSeatNumber(carriage.getCarriageNumber() + "-" + String.format("%02d", seatIndex));
                        seat.setIsAvailable(true);
                        seat.setRowNumber(row);
                        seat.setColumnNum(col);

                        seatRepository.save(seat);
                    }
                }
            }
            System.out.println("✅ Đã khởi tạo ghế cho tất cả toa tàu (Đã sửa logic)");
        }
    }

    private void initStations() {
        if (stationRepository.count() == 0) {
            Station hanoi = new Station();
            hanoi.setName("Ga Hà Nội");
            hanoi.setCode("HN");
            hanoi.setAddress("Số 1 Lê Duẩn, Hoàn Kiếm");
            hanoi.setCity("Hà Nội");
            hanoi.setProvince("Hà Nội");
            hanoi.setStatus("Hoạt động");

            Station danang = new Station();
            danang.setName("Ga Đà Nẵng");
            danang.setCode("DN");
            danang.setAddress("Số 200 Hải Phòng");
            danang.setCity("Đà Nẵng");
            danang.setProvince("Đà Nẵng");
            danang.setStatus("Hoạt động");

            Station hcm = new Station();
            hcm.setName("Ga Sài Gòn");
            hcm.setCode("SG");
            hcm.setAddress("Số 1 Nguyễn Thông, Quận 3");
            hcm.setCity("Hồ Chí Minh");
            hcm.setProvince("Hồ Chí Minh");
            hcm.setStatus("Hoạt động");

            Station hue = new Station();
            hue.setName("Ga Huế");
            hue.setCode("HUE");
            hue.setAddress("Số 2 Bùi Thị Xuân");
            hue.setCity("Huế");
            hue.setProvince("Thừa Thiên Huế");
            hue.setStatus("Hoạt động");

            Station nhatrang = new Station();
            nhatrang.setName("Ga Nha Trang");
            nhatrang.setCode("NT");
            nhatrang.setAddress("Số 26 Thái Nguyên");
            nhatrang.setCity("Nha Trang");
            nhatrang.setProvince("Khánh Hòa");
            nhatrang.setStatus("Hoạt động");

            stationRepository.saveAll(Arrays.asList(hanoi, danang, hcm, hue, nhatrang));
            System.out.println("✅ Đã khởi tạo 5 ga tàu");
        }
    }

    private void initRoutes() {
        if (routeRepository.count() == 0) {
            System.out.println("\n📍 initRoutes: Creating fully connected route network...");

            Station hn = stationRepository.findByCode("HN");
            Station dn = stationRepository.findByCode("DN");
            Station hue = stationRepository.findByCode("HUE");
            Station nt = stationRepository.findByCode("NT");
            Station sg = stationRepository.findByCode("SG");

            List<Route> routes = new ArrayList<>();

            // All directional pairs (5 choose 2 = 10 pairs × 2 directions = 20 routes)
            routes.add(new Route(hn, dn, 750.0));
            routes.add(new Route(dn, hn, 750.0));

            routes.add(new Route(hn, hue, 1300.0));
            routes.add(new Route(hue, hn, 1300.0));

            routes.add(new Route(hn, nt, 1700.0));
            routes.add(new Route(nt, hn, 1700.0));

            routes.add(new Route(hn, sg, 1750.0));
            routes.add(new Route(sg, hn, 1750.0));

            routes.add(new Route(dn, hue, 550.0));
            routes.add(new Route(hue, dn, 550.0));

            routes.add(new Route(dn, nt, 950.0));
            routes.add(new Route(nt, dn, 950.0));

            routes.add(new Route(dn, sg, 2000.0));
            routes.add(new Route(sg, dn, 2000.0));

            routes.add(new Route(hue, nt, 400.0));
            routes.add(new Route(nt, hue, 400.0));

            routes.add(new Route(hue, sg, 1450.0));
            routes.add(new Route(sg, hue, 1450.0));

            routes.add(new Route(nt, sg, 450.0));
            routes.add(new Route(sg, nt, 450.0));

            routeRepository.saveAll(routes);
            System.out.println("✅ initRoutes: Created " + routes.size() + " bidirectional routes (fully connected)");
        } else {
            System.out.println("⏭️  initRoutes: Routes already exist (" + routeRepository.count() + " total)");
        }
    }

    private void initSchedules() {
        if (scheduleRepository.count() == 0) {
            List<Train> trains = trainRepository.findAll();
            LocalDateTime now = LocalDateTime.now();

            Station hanoi = stationRepository.findByCode("HN");
            Station danang = stationRepository.findByCode("DN");
            Station hcm = stationRepository.findByCode("SG");

            // Lấy routes
            Route r1 = routeRepository.findByFromStationAndToStation(hanoi, danang).orElse(null);
            Route r2 = routeRepository.findByFromStationAndToStation(danang, hcm).orElse(null);
            Route r3 = routeRepository.findByFromStationAndToStation(hcm, hanoi).orElse(null);

            // Lấy routes ngược
            Route r1_return = routeRepository.findByFromStationAndToStation(danang, hanoi).orElse(null);
            Route r2_return = routeRepository.findByFromStationAndToStation(hcm, danang).orElse(null);
            Route r3_return = routeRepository.findByFromStationAndToStation(hanoi, hcm).orElse(null);

            if (r1 != null && r2 != null && r3 != null) {
                List<Schedule> schedulesToSave = new ArrayList<>();

                // Schedule 1: Hà Nội -> Đà Nẵng
                Schedule s1 = new Schedule();
                s1.setTrain(trains.get(0));
                s1.setDepartureStation(hanoi);
                s1.setArrivalStation(danang);
                s1.setRoute(r1);
                s1.setDepartureTime(now.plusDays(1).withHour(6).withMinute(0));
                s1.setArrivalTime(now.plusDays(1).withHour(15).withMinute(30));
                s1.setBasePrice(BigDecimal.valueOf(r1.getDistanceKm() * 700).setScale(0, java.math.RoundingMode.HALF_UP));
                s1.setCreatedAt(now);
                s1.setNotes("Chuyến tàu SE1");
                s1.setStopDuration(10);
                schedulesToSave.add(s1);

                // Schedule 1 Return: Đà Nẵng -> Hà Nội (departure = s1 arrival + 3h)
                Schedule s1_return = new Schedule();
                if (r1_return != null) s1_return.setRoute(r1_return);
                s1_return.setDepartureStation(danang);
                s1_return.setArrivalStation(hanoi);
                LocalDateTime s1_return_dep = s1.getArrivalTime().plusHours(3); // 15:30 + 3h = 18:30
                s1_return.setDepartureTime(s1_return_dep);
                double s1_return_distance = r1.getDistanceKm(); // Distance is same
                int s1_return_travel = Math.round((float) ((s1_return_distance / 60.0) * 60)); // ~750/60*60 = 750 min
                s1_return.setArrivalTime(s1_return_dep.plusMinutes(s1_return_travel + 10)); // +10 min stop
                s1_return.setBasePrice(BigDecimal.valueOf(Math.round(s1_return_distance * 700)));
                s1_return.setCreatedAt(now);
                s1_return.setNotes("Chuyến tàu SE1 - Chiều về");
                s1_return.setStopDuration(10);
                schedulesToSave.add(s1_return);

                // Schedule 2: Đà Nẵng -> Sài Gòn
                Schedule s2 = new Schedule();
                s2.setTrain(trains.get(1));
                s2.setDepartureStation(danang);
                s2.setArrivalStation(hcm);
                s2.setRoute(r2);
                s2.setDepartureTime(now.plusDays(2).withHour(8).withMinute(0));
                s2.setArrivalTime(now.plusDays(2).withHour(21).withMinute(20));
                s2.setBasePrice(BigDecimal.valueOf(r2.getDistanceKm() * 700).setScale(0, java.math.RoundingMode.HALF_UP));
                s2.setCreatedAt(now);
                s2.setNotes("Chuyến tàu SE3");
                s2.setStopDuration(15);
                schedulesToSave.add(s2);

                // Schedule 2 Return: Sài Gòn -> Đà Nẵng
                Schedule s2_return = new Schedule();
                if (r2_return != null) s2_return.setRoute(r2_return);
                s2_return.setDepartureStation(hcm);
                s2_return.setArrivalStation(danang);
                LocalDateTime s2_return_dep = s2.getArrivalTime().plusHours(3);
                s2_return.setDepartureTime(s2_return_dep);
                double s2_return_distance = r2.getDistanceKm();
                int s2_return_travel = Math.round((float) ((s2_return_distance / 60.0) * 60));
                s2_return.setArrivalTime(s2_return_dep.plusMinutes(s2_return_travel + 15));
                s2_return.setBasePrice(BigDecimal.valueOf(Math.round(s2_return_distance * 700)));
                s2_return.setCreatedAt(now);
                s2_return.setNotes("Chuyến tàu SE3 - Chiều về");
                s2_return.setStopDuration(15);
                schedulesToSave.add(s2_return);

                // Schedule 3: Sài Gòn -> Hà Nội
                Schedule s3 = new Schedule();
                s3.setTrain(trains.get(2));
                s3.setDepartureStation(hcm);
                s3.setArrivalStation(hanoi);
                s3.setRoute(r3);
                s3.setDepartureTime(now.plusDays(3).withHour(19).withMinute(30));
                s3.setArrivalTime(now.plusDays(4).withHour(16).withMinute(0));
                s3.setBasePrice(BigDecimal.valueOf(r3.getDistanceKm() * 700).setScale(0, java.math.RoundingMode.HALF_UP));
                s3.setCreatedAt(now);
                s3.setNotes("Chuyến tàu nhanh Sài Gòn - Hà Nội");
                s3.setStopDuration(5);
                schedulesToSave.add(s3);

                // Schedule 3 Return: Hà Nội -> Sài Gòn
                Schedule s3_return = new Schedule();
                if (r3_return != null) s3_return.setRoute(r3_return);
                s3_return.setDepartureStation(hanoi);
                s3_return.setArrivalStation(hcm);
                LocalDateTime s3_return_dep = s3.getArrivalTime().plusHours(3);
                s3_return.setDepartureTime(s3_return_dep);
                double s3_return_distance = r3.getDistanceKm();
                int s3_return_travel = Math.round((float) ((s3_return_distance / 60.0) * 60));
                s3_return.setArrivalTime(s3_return_dep.plusMinutes(s3_return_travel + 5));
                s3_return.setBasePrice(BigDecimal.valueOf(Math.round(s3_return_distance * 700)));
                s3_return.setCreatedAt(now);
                s3_return.setNotes("Chuyến tàu nhanh Hà Nội - Sài Gòn - Chiều về");
                s3_return.setStopDuration(5);
                schedulesToSave.add(s3_return);

                scheduleRepository.saveAll(schedulesToSave);
                System.out.println("✅ Đã khởi tạo 6 lịch trình (3 chiều đi + 3 chiều về) từ Routes");
            }
        }
    }

    private void initScheduleStops() {
        if (scheduleStopRepository.count() == 0) {
            List<Schedule> schedules = scheduleRepository.findAll();
            Station hue = stationRepository.findByCode("HUE");

            for (Schedule schedule : schedules) {
                if (schedule.getDepartureStation().getCode().equals("HN") &&
                        schedule.getArrivalStation().getCode().equals("DN")) {
                    // Hà Nội -> Đà Nẵng với điểm dừng tại Huế
                    ScheduleStop stop1 = new ScheduleStop();
                    stop1.setSchedule(schedule);
                    stop1.setStation(hue);
                    stop1.setArrivalTime(schedule.getDepartureTime().plusHours(8));
                    stop1.setDepartureTime(schedule.getDepartureTime().plusHours(8).plusMinutes(15));
                    stop1.setStopOrder(1);
                    stop1.setDistanceFromStart(688.0);
                    scheduleStopRepository.save(stop1);
                }
            }
            System.out.println("✅ Đã khởi tạo điểm dừng lịch trình");
        }
    }
}