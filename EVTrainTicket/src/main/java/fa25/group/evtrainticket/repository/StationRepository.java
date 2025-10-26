package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Station,Integer> {
    boolean existsByCodeAndName(String code, String name);
}
