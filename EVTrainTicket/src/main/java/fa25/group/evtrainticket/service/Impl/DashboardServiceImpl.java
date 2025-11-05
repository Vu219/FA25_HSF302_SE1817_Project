package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.dto.StatsResponse;
import fa25.group.evtrainticket.dto.WeeklyStatsResponse;
import fa25.group.evtrainticket.repository.PaymentRepository;
import fa25.group.evtrainticket.repository.TicketRepository;
import fa25.group.evtrainticket.repository.UserRepository;
import fa25.group.evtrainticket.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired
    private UserRepository  userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public StatsResponse formatStats(String title, long currentValue, long previousValue) {
        String value = String.format("%,d", currentValue);

        double percentValue;
        if(previousValue == 0){
            percentValue = (currentValue ==0) ? 0 :100;
        }
        else{
            percentValue = ((double) (currentValue-previousValue) / previousValue) *100;
        }

        String change = (percentValue >= 0 ? "+" : "") + String.format("%.0f", percentValue)  + "%";
        boolean isGrowing = percentValue > 0;
        return new StatsResponse(title, value, change, isGrowing);
    }

    @Override
    public StatsResponse getTotalUsers(LocalDate startDate, LocalDate endDate) {
        long previousValue = userRepository.countUsers(startDate,endDate);
        long currentValue = userRepository.countTotalUsers();
        return formatStats("Tổng người dùng", currentValue, previousValue);
    }

    @Override
    public StatsResponse getTotalTickets(LocalDate startDate, LocalDate endDate) {
        long previousValue = ticketRepository.countTickets(startDate,endDate);
        long currentValue = ticketRepository.countTotalTickets();
        return formatStats("Tổng số vé", currentValue, previousValue);
    }

    @Override
    public StatsResponse getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        long previousValue = paymentRepository.getRevenue(startDate, endDate);
        long currentValue = paymentRepository.getTotalRevenue();
        return formatStats("Doanh thu tháng", currentValue, previousValue);
    }

    @Override
    public List<StatsResponse> getAllStats() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(2);

        return List.of(
                getTotalUsers(startDate, endDate),
                getTotalTickets(startDate, endDate),
                getTotalRevenue(startDate, endDate)
        );
    }

    public List<WeeklyStatsResponse> getAllStatsWeekly(LocalDate startDate, LocalDate endDate){
        List<WeeklyStatsResponse> weeklyStats = new ArrayList<>();
        long weeks = ChronoUnit.WEEKS.between(startDate, endDate) +1;
        LocalDate startWeek = startDate;
        long previousUser = 0;
        long previousTicket = 0;
        long previousRevenue = 0;

        for(int i =0; i<weeks; i++){
            LocalDate endWeek = startWeek.plusDays(6);
            if(endWeek.isAfter(endDate)){
                endWeek = endDate;
            }
            long userCount = userRepository.countUsers(startWeek, endWeek);
            long ticketCount = ticketRepository.countTickets(startWeek, endWeek);
            long revenueCount = paymentRepository.getRevenue(startWeek, endWeek);

            List<StatsResponse> stats = List.of(
                    formatStats("Tổng số người dùng", userCount, previousUser),
                    formatStats("Tổng số vé", ticketCount, previousTicket),
                    formatStats("Doanh thu tuần", revenueCount, previousRevenue)
            );
            String weekRange = startWeek.toString() + " -> " + endWeek.toString();
            weeklyStats.add(new WeeklyStatsResponse(weekRange, stats));

            previousUser = userCount;
            previousTicket = ticketCount;
            previousRevenue = revenueCount;
            startWeek = startWeek.plusWeeks(1);
        }
        return weeklyStats;
    }
}
