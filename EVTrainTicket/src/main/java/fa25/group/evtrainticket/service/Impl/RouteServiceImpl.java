package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Route;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.repository.RouteRepository;
import fa25.group.evtrainticket.service.RouteService;
import fa25.group.evtrainticket.service.RouteGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final RouteRepository routeRepository;
    private final RouteGeneratorService routeGeneratorService;

    @Override
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    public Route getRouteById(Long id) {
        return routeRepository.findById(id).orElse(null);
    }

    @Override
    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    @Override
    public Route findByStations(Station fromStation, Station toStation) {
        return routeRepository.findByFromStationAndToStation(fromStation, toStation).orElse(null);
    }

    @Override
    public Map<String, Object> calculateRouteDistance(Station fromStation, Station toStation) {
        Map<String, Object> result = new HashMap<>();

        // 1️⃣ Thử tìm direct route trước
        Route directRoute = findByStations(fromStation, toStation);
        if (directRoute != null) {
            result.put("distance_km", directRoute.getDistanceKm());
            result.put("routeId", directRoute.getId());
            result.put("isIndirect", false);
            return result;
        }

        // 2️⃣ Nếu không có direct route, dùng RouteGeneratorService tìm đường gián tiếp
        List<Route> path = routeGeneratorService.findShortestPath(fromStation, toStation);
        if (path != null && !path.isEmpty()) {
            double totalDistance = path.stream()
                    .mapToDouble(Route::getDistanceKm)
                    .sum();

            result.put("distance_km", totalDistance);
            result.put("routeId", null);  // Không có direct route
            result.put("isIndirect", true);
            result.put("path", path);  // Danh sách segments
            return result;
        }

        // 3️⃣ Không tìm thấy đường nào
        result.put("distance_km", null);
        result.put("routeId", null);
        result.put("isIndirect", false);
        result.put("error", "Không tìm thấy tuyến đường từ " + fromStation.getName() + " đến " + toStation.getName());
        return result;
    }
}

