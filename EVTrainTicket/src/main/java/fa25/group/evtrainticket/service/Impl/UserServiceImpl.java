package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.repository.UserRepository;
import fa25.group.evtrainticket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public boolean addUserAccount(User user) throws Exception {
        if (existsByEmail(user.getEmail())) {
            throw new Exception("Email đã tồn tại trong hệ thống");
        }
        return userRepository.save(user) != null;
    }

    @Override
    public User getUserAccount(String email, String password) throws Exception {
        User user = userRepository.findByEmailAndPassword(email, password);
        if (user == null) {
            throw new Exception("Email hoặc mật khẩu không đúng");
        }
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updatePassword(String email, String newPassword) throws Exception {
        User user = findByEmail(email);

        if (newPassword.length() < 6) {
            throw new Exception("Mật khẩu phải có ít nhất 6 ký tự");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new Exception("Người dùng với email " + email + " không tồn tại");
        }
        return userOptional.get();
    }
}