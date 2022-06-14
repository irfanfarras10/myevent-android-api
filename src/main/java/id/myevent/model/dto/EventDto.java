package id.myevent.model.dto;

import lombok.Data;

/** Event DTO. */
@Data
public class EventDto {
  private String name;
  private String description;
  private int dateTimeEventStart;
  private int dateTimeEventEnd;
  private String location;
  private byte[] bannerPhoto;
  private int dateTimeRegistrationStart;
  private int dateTimeRegistrationEnd;
  private long eventStatusId;
  private long eventCategoryId;
  private long eventPaymentCategoryId;
  private long eventOrganizerId;
}
