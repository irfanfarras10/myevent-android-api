package id.myevent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class UserDto {
  private long id;
  private String username;
  private String email;
  private String password;
  private String organizerName;
  private String phoneNumber;
}
