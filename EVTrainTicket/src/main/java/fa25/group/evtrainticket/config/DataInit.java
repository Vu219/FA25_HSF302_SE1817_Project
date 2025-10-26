package fa25.group.evtrainticket.config;


import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.repository.UserRepository;
import fa25.group.evtrainticket.entity.Train;
import fa25.group.evtrainticket.entity.CarriageType;
import fa25.group.evtrainticket.service.TrainService;
import fa25.group.evtrainticket.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import fa25.group.evtrainticket.entity.Carriage;
import fa25.group.evtrainticket.service.CarriageService;
import fa25.group.evtrainticket.service.CarriageTypeService;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInit implements CommandLineRunner {
    UserService userService;
    UserRepository userRepository;
    TrainService trainService;
    CarriageTypeService carriageTypeService;
    CarriageService carriageService;

    @Override
    public void run(String... args) throws Exception {
        initUsers();
        initTrains();
        initCarriageTypes();
        initCarriages();
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
        if (trainService.getAllTrains().isEmpty()) {
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
}
