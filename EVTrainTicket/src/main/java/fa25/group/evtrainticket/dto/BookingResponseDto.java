package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Integer bookingId;
    private String bookingCode;
    private String status;
    private LocalDateTime bookingDate;
    private Double totalAmount;
    private String notes;
    private String userFullName;
    private String userEmail;
    private List<TicketDto> tickets;
    private List<PaymentDto> payments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {
        private Integer scheduleId;
        private String trainName;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketDto {
        private Integer ticketId;
        private String ticketCode;
        private String seatNumber;
        private String carriageName;
        private String seatType;
        private Double price;
        private String status;
        private ScheduleDto schedule;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDto {
        private Integer paymentId;
        private String transactionCode;
        private String status;
        private Double amount;
        private String paymentMethod;
        private LocalDateTime paymentDate;
        private String notes;
    }
}
