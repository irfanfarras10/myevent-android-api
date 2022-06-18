package id.myevent.model.apiresponse;

import id.myevent.model.dao.*;
import lombok.Data;

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
}
