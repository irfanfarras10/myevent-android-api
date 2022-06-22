package id.myevent.model.dto;

import lombok.Data;

/**Event Guest Dto.*/
@Data
public class EventGuestDto {
  private String name;
  private String phoneNumber;
  private String email;
  private Boolean alreadyShared;
}
