package fa25.group.evtrainticket.config;


import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.repository.OTPRepository;
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
    OTPRepository otpRepository;

    @Override
    public void run(String... args) throws Exception {
        initUsers();
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

            System.out.println("Đã khởi tạo dữ liệu người dùng mẫu!");
        }

    }
}
