package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);

    public User findByEmailAndPassword(String email, String password);
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE CAST(u.createdAt AS date) BETWEEN :startDate AND :endDate")
    long countUsers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();
}
