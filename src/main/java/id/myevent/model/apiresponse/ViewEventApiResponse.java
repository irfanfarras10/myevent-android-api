package id.myevent.model.apiresponse;

import id.myevent.model.dao.*;
import lombok.Data;

import java.util.List;

/** View Event Api Response. */
@Data
public class ViewEventApiResponse {
  private String name;
  private String description;
  private Integer dateTimeEventStart;
  private Integer dateTimeEventEnd;
  private String venue;
  private String bannerPhoto;
  private Integer dateTimeRegistrationStart;
  private Integer dateTimeRegistrationEnd;
  private EventStatusDao eventStatus;
  private EventCategoryDao eventCategory;
  private EventVenueCategoryDao eventVenueCategory;
  private EventPaymentCategoryDao eventPaymentCategory;
  private EventOrganizerDao eventOrganizer;
  private List<EventContactPersonDao> eventContactPerson;
  private List<TicketDao> ticket;
}
