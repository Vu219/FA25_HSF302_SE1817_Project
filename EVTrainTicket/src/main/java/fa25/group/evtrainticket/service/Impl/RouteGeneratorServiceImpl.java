package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Route;
import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.repository.RouteRepository;
import fa25.group.evtrainticket.service.RouteGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteGeneratorServiceImpl implements RouteGeneratorService {
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public int generateAllPossibleRoutes() {
        List<Route> allRoutes = routeRepository.findAll();
        if (allRoutes.isEmpty()) {
            return 0;
        }

        // Build graph từ các segments liền kề
        Map<Integer, List<Route>> graph = new HashMap<>();
        for (Route route : allRoutes) {
            graph.computeIfAbsent(route.getFromStation().getStationID(), k -> new ArrayList<>())
                    .add(route);
        }

        Set<Route> generatedRoutes = new HashSet<>();
        int newRoutesCount = 0;

        // Từ mỗi station, tìm tất cả stations khác có thể đến được
        for (Station fromStation : getAllStations(allRoutes)) {
            Map<Integer, RouteInfo> reachable = findAllReachable(fromStation, graph);

            if (!reachable.isEmpty()) {
                System.out.println("   From " + fromStation.getName() + ": reachable " + reachable.size() + " stations");
            }

            for (Integer toStationId : reachable.keySet()) {
                if (fromStation.getStationID().equals(toStationId)) {
                    continue;  // Bỏ qua tuyến cùng station
                }

                RouteInfo info = reachable.get(toStationId);

                // Kiểm tra route đã tồn tại chưa
                Route existing = routeRepository
                        .findByFromStationAndToStation(fromStation, info.endStation)
                        .orElse(null);

                if (existing == null) {
                    // Tạo route mới
                    Route newRoute = new Route(
                            fromStation,
                            info.endStation,
                            info.totalDistance
                    );
                    generatedRoutes.add(newRoute);
                    newRoutesCount++;
                    System.out.println("     ➕ Generated: " + fromStation.getName() + " → " + info.endStation.getName() + " (" + info.totalDistance + " km)");
                }
            }
        }

        // Save tất cả routes mới
        if (!generatedRoutes.isEmpty()) {
            routeRepository.saveAll(generatedRoutes);
            System.out.println("✅ Generated " + newRoutesCount + " new routes from segments");
        }

        return newRoutesCount;
    }

    @Override
    public List<Route> findShortestPath(Station fromStation, Station toStation) {
        List<Route> allRoutes = routeRepository.findAll();

        // Build graph
        Map<Integer, List<Route>> graph = new HashMap<>();
        for (Route route : allRoutes) {
            graph.computeIfAbsent(route.getFromStation().getStationID(), k -> new ArrayList<>())
                    .add(route);
        }

        // BFS để tìm shortest path
        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> visited = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();

        queue.add(fromStation.getStationID());
        visited.put(fromStation.getStationID(), 0);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();

            if (current.equals(toStation.getStationID())) {
                // Reconstruct path
                return reconstructPath(parent, fromStation, toStation, allRoutes);
            }

            List<Route> neighbors = graph.getOrDefault(current, new ArrayList<>());
            for (Route route : neighbors) {
                Integer nextStationId = route.getToStation().getStationID();
                if (!visited.containsKey(nextStationId)) {
                    visited.put(nextStationId, visited.get(current) + 1);
                    parent.put(nextStationId, current);
                    queue.add(nextStationId);
                }
            }
        }

        return new ArrayList<>();  // Không tìm thấy đường
    }

    /**
     * Find all reachable stations từ một station
     * @return Map<toStationId, RouteInfo>
     */
    private Map<Integer, RouteInfo> findAllReachable(Station fromStation, Map<Integer, List<Route>> graph) {
        Map<Integer, RouteInfo> reachable = new HashMap<>();
        Map<Integer, Double> distances = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(fromStation.getStationID());
        distances.put(fromStation.getStationID(), 0.0);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            double currentDistance = distances.get(current);

            List<Route> routes = graph.getOrDefault(current, new ArrayList<>());
            for (Route route : routes) {
                Integer nextId = route.getToStation().getStationID();
                double nextDistance = currentDistance + route.getDistanceKm();

                // Nếu chưa visit hoặc tìm được đường ngắn hơn
                if (!distances.containsKey(nextId) || distances.get(nextId) > nextDistance) {
                    distances.put(nextId, nextDistance);
                    reachable.put(nextId, new RouteInfo(route.getToStation(), nextDistance));
                    queue.add(nextId);  // Re-add để process neighbors với distance mới
                }
            }
        }

        return reachable;
    }

    private List<Route> reconstructPath(Map<Integer, Integer> parent, Station fromStation,
                                         Station toStation, List<Route> allRoutes) {
        List<Route> path = new ArrayList<>();
        Integer current = toStation.getStationID();

        while (!current.equals(fromStation.getStationID())) {
            Integer prev = parent.get(current);
            if (prev == null) break;

            // Tìm route từ prev → current
            for (Route route : allRoutes) {
                if (route.getFromStation().getStationID().equals(prev) &&
                    route.getToStation().getStationID().equals(current)) {
                    path.add(0, route);
                    break;
                }
            }

            current = prev;
        }

        return path;
    }

    private List<Station> getAllStations(List<Route> allRoutes) {
        return allRoutes.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getFromStation(), r.getToStation()))
                .distinct()
                .collect(Collectors.toList());
    }

    // Helper class
    private static class RouteInfo {
        Station endStation;
        Double totalDistance;

        RouteInfo(Station endStation, Double totalDistance) {
            this.endStation = endStation;
            this.totalDistance = totalDistance;
        }
    }
}

