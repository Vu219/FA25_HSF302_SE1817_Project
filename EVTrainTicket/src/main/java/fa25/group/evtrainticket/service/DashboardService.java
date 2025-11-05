package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.dto.StatsResponse;
import fa25.group.evtrainticket.dto.WeeklyStatsResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    StatsResponse formatStats(String title, long currentValue, long previousValue);
    StatsResponse getTotalUsers(LocalDate startDate, LocalDate endDate);
    StatsResponse getTotalTickets(LocalDate startDate, LocalDate endDate);
    StatsResponse getTotalRevenue(LocalDate startDate, LocalDate endDate);
    List<StatsResponse> getAllStats();
    List<WeeklyStatsResponse> getAllStatsWeekly(LocalDate startDate, LocalDate endDate);
}
