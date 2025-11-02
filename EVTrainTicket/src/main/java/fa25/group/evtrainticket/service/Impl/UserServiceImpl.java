package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.repository.UserRepository;
import fa25.group.evtrainticket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUserById(Integer userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findById(userId).get();
    }

    @Override
    public User updateUser(Integer userId, User newUser) {
        User updateUser = getUserById(userId);
        updateUser.setFullName(newUser.getFullName());
        updateUser.setEmail(newUser.getEmail());
        updateUser.setPassword(newUser.getPassword());
        updateUser.setPhone(newUser.getPhone());
        updateUser.setRole(newUser.getRole());
        return userRepository.save(updateUser);
    }
}