package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.repository.UserRepository;
import fa25.group.evtrainticket.dto.UserRegistrationDto;
import fa25.group.evtrainticket.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(UserRegistrationDto registrationDto) {
        // Check if user already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhone(registrationDto.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // Create new user
        User user = new User();
        user.setFullName(registrationDto.getFullName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword()); // In production, hash the password
        user.setPhone(registrationDto.getPhone());
        user.setRole("CUSTOMER");
        user.setCreateAt(LocalDateTime.now());
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // In production, compare hashed passwords
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        return user;
    }

    @Override
    public User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
