package fa25.group.evtrainticket.config;

import fa25.group.evtrainticket.repository.*;
import fa25.group.evtrainticket.entity.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInit {

    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final CarriageTypeRepository carriageTypeRepository;
    private final CarriageRepository carriageRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void initData() {
        if (stationRepository.count() > 0) {
            return; // Data already exists, skip initialization
        }

        // Initialize Demo Users first
        User demoUser = createUser("Demo User", "demo@example.com", "password123", "0123456789", "CUSTOMER", "ACTIVE");
        User adminUser = createUser("Admin User", "admin@example.com", "admin123", "0987654321", "ADMIN", "ACTIVE");

        userRepository.saveAll(List.of(demoUser, adminUser));

        // Initialize Stations
        Station hanoi = createStation("Hanoi Station", "HN", "120 Le Duan Street", "Hanoi", "Hanoi", "Active");
        Station hcm = createStation("Ho Chi Minh Station", "HCM", "1 Nguyen Thong Street", "Ho Chi Minh City", "Ho Chi Minh", "Active");
        Station danang = createStation("Da Nang Station", "DN", "202 Hai Phong Street", "Da Nang", "Da Nang", "Active");
        Station hue = createStation("Hue Station", "HUE", "Bui Thi Xuan Street", "Hue", "Thua Thien Hue", "Active");
        Station nhatrang = createStation("Nha Trang Station", "NT", "17 Thai Nguyen Street", "Nha Trang", "Khanh Hoa", "Active");

        stationRepository.saveAll(List.of(hanoi, hcm, danang, hue, nhatrang));

        // Initialize Seat Types
        SeatType hardSeat = createSeatType("Hard Seat", new BigDecimal("1.0"), "Standard hard seat");
        SeatType softSeat = createSeatType("Soft Seat", new BigDecimal("1.5"), "Comfortable soft seat");
        SeatType sleeper = createSeatType("Sleeper", new BigDecimal("2.0"), "Sleeping berth");
        SeatType vip = createSeatType("VIP", new BigDecimal("3.0"), "VIP class with premium amenities");

        seatTypeRepository.saveAll(List.of(hardSeat, softSeat, sleeper, vip));

        // Initialize Carriage Types
        CarriageType economyCarriage = createCarriageType("Economy", 64, "Standard economy carriage", new BigDecimal("1.0"));
        CarriageType businessCarriage = createCarriageType("Business", 48, "Business class carriage", new BigDecimal("1.5"));
        CarriageType sleeperCarriage = createCarriageType("Sleeper", 32, "Sleeping carriage", new BigDecimal("2.0"));
        CarriageType vipCarriage = createCarriageType("VIP", 24, "VIP carriage with premium service", new BigDecimal("3.0"));

        carriageTypeRepository.saveAll(List.of(economyCarriage, businessCarriage, sleeperCarriage, vipCarriage));

        // Initialize Trains
        Train se1 = createTrain("SE1", "Reunification Express", 500, "Active", "High-speed train connecting North to South");
        Train se3 = createTrain("SE3", "North-South Express", 450, "Active", "Express service between major cities");
        Train se7 = createTrain("SE7", "Coastal Express", 400, "Active", "Scenic coastal route train");

        trainRepository.saveAll(List.of(se1, se3, se7));

        // Initialize Carriages for SE1 train
        List<Carriage> se1Carriages = new ArrayList<>();
        se1Carriages.add(createCarriage(se1, 1, economyCarriage, 1, 64));
        se1Carriages.add(createCarriage(se1, 2, economyCarriage, 2, 64));
        se1Carriages.add(createCarriage(se1, 3, businessCarriage, 3, 48));
        se1Carriages.add(createCarriage(se1, 4, sleeperCarriage, 4, 32));
        se1Carriages.add(createCarriage(se1, 5, vipCarriage, 5, 24));

        carriageRepository.saveAll(se1Carriages);

        // Initialize Seats for carriages
        List<Seat> seats = new ArrayList<>();
        for (Carriage carriage : se1Carriages) {
            seats.addAll(createSeatsForCarriage(carriage, hardSeat, softSeat, sleeper, vip));
        }
        seatRepository.saveAll(seats);

        // Initialize Schedules
        List<Schedule> schedules = new ArrayList<>();

        // Hanoi to Ho Chi Minh City
        schedules.add(createSchedule(hanoi, hcm, se1,
            LocalDateTime.of(2025, 10, 25, 6, 0),
            LocalDateTime.of(2025, 10, 26, 18, 30),
            1726.0, 1950, new BigDecimal("500000"), "Active", "Daily service"));

        // Ho Chi Minh City to Hanoi
        schedules.add(createSchedule(hcm, hanoi, se1,
            LocalDateTime.of(2025, 10, 25, 19, 30),
            LocalDateTime.of(2025, 10, 27, 8, 0),
            1726.0, 1950, new BigDecimal("500000"), "Active", "Daily return service"));

        // Hanoi to Da Nang
        schedules.add(createSchedule(hanoi, danang, se3,
            LocalDateTime.of(2025, 10, 25, 8, 0),
            LocalDateTime.of(2025, 10, 25, 20, 0),
            791.0, 720, new BigDecimal("300000"), "Active", "Popular route"));

        // Da Nang to Ho Chi Minh City
        schedules.add(createSchedule(danang, hcm, se7,
            LocalDateTime.of(2025, 10, 25, 14, 0),
            LocalDateTime.of(2025, 10, 26, 6, 0),
            935.0, 960, new BigDecimal("350000"), "Active", "Coastal scenic route"));

        // Hanoi to Hue
        schedules.add(createSchedule(hanoi, hue, se3,
            LocalDateTime.of(2025, 10, 25, 22, 0),
            LocalDateTime.of(2025, 10, 26, 10, 0),
            688.0, 720, new BigDecimal("280000"), "Active", "Overnight service"));

        scheduleRepository.saveAll(schedules);

        System.out.println("Test data initialized successfully!");
    }

    private Station createStation(String name, String code, String address, String city, String province, String status) {
        Station station = new Station();
        station.setName(name);
        station.setCode(code);
        station.setAddress(address);
        station.setCity(city);
        station.setProvince(province);
        station.setStatus(status);
        return station;
    }

    private SeatType createSeatType(String typeName, BigDecimal priceMultiplier, String description) {
        SeatType seatType = new SeatType();
        seatType.setTypeName(typeName);
        seatType.setPriceMultiplier(priceMultiplier);
        seatType.setDescription(description);
        return seatType;
    }

    private CarriageType createCarriageType(String typeName, Integer seatCount, String description, BigDecimal priceMultiplier) {
        CarriageType carriageType = new CarriageType();
        carriageType.setTypeName(typeName);
        carriageType.setSeatCount(seatCount);
        carriageType.setDescription(description);
        carriageType.setPriceMultiplier(priceMultiplier);
        return carriageType;
    }

    private Train createTrain(String trainNumber, String trainName, Integer capacity, String status, String notes) {
        Train train = new Train();
        train.setTrainNumber(trainNumber);
        train.setTrainName(trainName);
        train.setCapacity(capacity);
        train.setStatus(status);
        train.setNotes(notes);
        return train;
    }

    private Carriage createCarriage(Train train, Integer carriageNumber, CarriageType carriageType, Integer position, Integer totalSeats) {
        Carriage carriage = new Carriage();
        carriage.setTrain(train);
        carriage.setCarriageNumber(carriageNumber);
        carriage.setCarriageType(carriageType);
        carriage.setPosition(position);
        carriage.setTotalSeats(totalSeats);
        return carriage;
    }

    private List<Seat> createSeatsForCarriage(Carriage carriage, SeatType hardSeat, SeatType softSeat, SeatType sleeper, SeatType vip) {
        List<Seat> seats = new ArrayList<>();
        CarriageType carriageType = carriage.getCarriageType();

        // Determine seat type based on carriage type
        SeatType seatType = switch (carriageType.getTypeName()) {
            case "Economy" -> hardSeat;
            case "Business" -> softSeat;
            case "Sleeper" -> sleeper;
            case "VIP" -> vip;
            default -> hardSeat;
        };

        // Create seats based on carriage capacity
        int seatCount = carriageType.getSeatCount();
        int seatsPerRow = 4; // Assuming 4 seats per row (2A, 2B, 2C, 2D)

        for (int i = 1; i <= seatCount; i++) {
            Seat seat = new Seat();
            seat.setCarriage(carriage);
            seat.setSeatType(seatType);
            seat.setIsAvailable(true);

            int row = ((i - 1) / seatsPerRow) + 1;
            int column = ((i - 1) % seatsPerRow) + 1;

            char seatLetter = (char) ('A' + column - 1);
            seat.setSeatNumber(row + "" + seatLetter);
            seat.setRowNumber(row);
            seat.setColumnPosition(String.valueOf(seatLetter));

            seats.add(seat);
        }

        return seats;
    }

    private Schedule createSchedule(Station departureStation, Station arrivalStation, Train train,
                                 LocalDateTime departureTime, LocalDateTime arrivalTime,
                                 Double distanceKm, Integer estimatedTime, BigDecimal basePrice,
                                 String status, String notes) {
        Schedule schedule = new Schedule();
        schedule.setDepartureStation(departureStation);
        schedule.setArrivalStation(arrivalStation);
        schedule.setTrain(train);
        schedule.setDepartureTime(departureTime);
        schedule.setArrivalTime(arrivalTime);
        schedule.setDistanceKm(distanceKm);
        schedule.setEstimatedTime(estimatedTime);
        schedule.setBasePrice(basePrice);
        schedule.setOrigin(departureStation.getName());
        schedule.setDestination(arrivalStation.getName());
        schedule.setStatus(status);
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setNotes(notes);
        return schedule;
    }

    private User createUser(String name, String email, String password, String phone, String role, String status) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(status);
        user.setCreateAt(LocalDateTime.now());
        return user;
    }
}
