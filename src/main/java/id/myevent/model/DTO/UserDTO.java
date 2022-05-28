package id.myevent.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private long id;
    private String username;
    private String email;
    private String password;
    private String organizerName;
    private String phoneNumber;
}
