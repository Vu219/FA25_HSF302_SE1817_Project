package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);

    public User findByEmailAndPassword(String email, String password);
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);
}
