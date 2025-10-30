package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.dto.UserRegistrationDto;
import fa25.group.evtrainticket.entity.User;

public interface UserService {
    User registerUser(UserRegistrationDto registrationDto);
    User loginUser(String email, String password);
    User findById(Integer userId);
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
