package id.myevent.model.dto;

import lombok.Data;

/** Event DTO. */
@Data
public class EventDto {
  private String name;
  private String description;
  private Long dateTimeEventStart;
  private Long dateTimeEventEnd;
  private String venue;
  private byte[] bannerPhoto;
  private String bannerPhotoName;
  private String bannerPhotoType;
  private Long dateTimeRegistrationStart;
  private Long dateTimeRegistrationEnd;
  private Long eventStatusId;
  private Long eventCategoryId;
  private Long eventVenueCategoryId;
  private Long eventPaymentCategoryId;
  private Long eventOrganizerId;
}
