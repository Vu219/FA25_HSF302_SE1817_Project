package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.User;

public interface UserService {
    public boolean addUserAccount(User user) throws Exception;
    public User getUserAccount(String email, String password) throws Exception;
    public boolean existsByEmail(String email);
    public void updatePassword(String email, String newPassword) throws Exception;
    public User findByEmail(String email) throws Exception;
}
