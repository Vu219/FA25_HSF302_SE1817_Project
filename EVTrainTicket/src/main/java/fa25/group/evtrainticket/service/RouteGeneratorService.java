package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Route;
import fa25.group.evtrainticket.entity.Station;

import java.util.List;
import java.util.Map;

/**
 * Service để tự động generate tất cả routes có thể từ các segments liền kề
 * Ví dụ: Nếu có HCM→NT, NT→DN, DN→HN
 * Thì tự động generate: HCM→DN, HCM→HN, NT→HN, etc.
 */
public interface RouteGeneratorService {
    /**
     * Generate tất cả routes từ các segments liền kề
     * @return số routes được tạo mới
     */
    int generateAllPossibleRoutes();

    /**
     * Find shortest path từ từ station này tới station khác
     * @return List các routes theo đúng thứ tự
     */
    List<Route> findShortestPath(Station fromStation, Station toStation);
}

