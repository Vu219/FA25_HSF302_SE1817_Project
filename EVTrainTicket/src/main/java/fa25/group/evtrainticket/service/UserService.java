package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.entity.User;

import java.util.List;

public interface UserService {
    public boolean addUserAccount(User user) throws Exception;
    public User getUserAccount(String email, String password) throws Exception;
    public boolean existsByEmail(String email);
    public void updatePassword(String email, String newPassword) throws Exception;
    public User findByEmail(String email) throws Exception;
    public List<User> getAllUsers();
    public void deleteUserById(Integer userId);
    public User saveUser(User user);
    public User getUserById(Integer userId);
    public User updateUser(Integer userId, User newUser);

}
