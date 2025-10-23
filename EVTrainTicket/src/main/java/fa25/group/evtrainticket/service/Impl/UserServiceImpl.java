package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.User;
import fa25.group.evtrainticket.repository.UserRepository;
import fa25.group.evtrainticket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public boolean addUserAccount(User user) {
        return userRepository.save(user) != null;
    }

    @Override
    public User getUserAccount(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }
}
