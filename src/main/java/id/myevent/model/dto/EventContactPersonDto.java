package id.myevent.model.dto;

import lombok.Data;

/** Event Contact Person DAO. */
@Data
public class EventContactPersonDto {
  private String name;
  private String contact;
  private Long eventSocialMediaId;
}
