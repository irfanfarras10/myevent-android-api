package id.myevent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Event Organizer DTO. */
@Data
@AllArgsConstructor
public class EventOrganizerDto {
  private long id;
  private String username;
  private String email;
  private String password;
  private String organizerName;
  private String phoneNumber;
}
