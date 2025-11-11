package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Route;
import fa25.group.evtrainticket.entity.Station;

import java.util.List;
import java.util.Map;

public interface RouteService {
    List<Route> getAllRoutes();
    Route getRouteById(Long id);
    Route saveRoute(Route route);
    void deleteRoute(Long id);
    Route findByStations(Station fromStation, Station toStation);

    /**
     * Tính toán khoảng cách và thời gian tổng cộng từ ga đi đến ga đến
     * Nếu có direct route thì lấy luôn, nếu không có thì tìm đường qua các segments liền kề
     * @return Map chứa: distance_km, duration_min, routeId (nếu direct), isIndirect
     */
    Map<String, Object> calculateRouteDistance(Station fromStation, Station toStation);
}

