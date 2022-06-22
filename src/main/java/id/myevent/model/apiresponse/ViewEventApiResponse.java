package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventCategoryDao;
import id.myevent.model.dao.EventContactPersonDao;
import id.myevent.model.dao.EventGuestDao;
import id.myevent.model.dao.EventOrganizerDao;
import id.myevent.model.dao.EventPaymentCategoryDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.model.dao.EventVenueCategoryDao;
import id.myevent.model.dao.TicketDao;
import java.util.List;
import lombok.Data;

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
  private List<EventGuestDao> eventGuest;
}
