package fa25.group.evtrainticket.repository;

import fa25.group.evtrainticket.entity.Route;
import fa25.group.evtrainticket.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByFromStationAndToStation(Station fromStation, Station toStation);
}

