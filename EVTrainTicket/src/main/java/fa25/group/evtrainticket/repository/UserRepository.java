package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByPhoneAndPassword(String phone, String password);
}
