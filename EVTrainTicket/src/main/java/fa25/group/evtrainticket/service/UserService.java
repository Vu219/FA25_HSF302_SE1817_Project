package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public boolean addUserAccount(User user);
    public User getUserAccount(String email, String password);
}
