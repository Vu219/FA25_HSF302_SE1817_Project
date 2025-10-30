package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    private Integer userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;
    private Integer scheduleId;
    private List<Integer> selectedSeatIds;
    private String ticketType;
    private String paymentMethod;
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private String notes;
}
