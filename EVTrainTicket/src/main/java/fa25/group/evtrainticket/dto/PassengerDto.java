package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDto {    // thông tin khách đi cùng nguòi đặt vé
    private String fullName;
    private String idCard;
    private String ticketType;
}
