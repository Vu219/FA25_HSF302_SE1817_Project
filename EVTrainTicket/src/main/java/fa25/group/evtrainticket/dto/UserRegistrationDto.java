package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
}
