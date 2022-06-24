package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventCategoryDao;
import id.myevent.model.dao.EventOrganizerDao;
import id.myevent.model.dao.EventPaymentCategoryDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.model.dao.EventVenueCategoryDao;
import lombok.Data;

/**
 * View Event List.
 */
@Data
public class ViewEventListApiResponse {
  private String name;
  private String description;
  private Long dateTimeEventStart;
  private Long dateTimeEventEnd;
  private String venue;
  private String bannerPhoto;
  private EventStatusDao eventStatus;
  private EventCategoryDao eventCategory;
  private EventVenueCategoryDao eventVenueCategory;
  private EventPaymentCategoryDao eventPaymentCategory;
  private EventOrganizerDao eventOrganizer;
}
