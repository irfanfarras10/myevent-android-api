package id.myevent.service;

import id.myevent.model.apiresponse.ViewEventCategoryApiResponse;
import id.myevent.model.apiresponse.ViewEventSocialMediaApiResponse;
import id.myevent.model.dao.EventCategoryDao;
import id.myevent.model.dao.EventSocialMediaDao;
import id.myevent.repository.EventSocialMediaRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Event Social Media Service.
 */
@Service
public class EventSocialMediaService {

  @Autowired
  EventSocialMediaRepository eventSocialMediaRepository;

  /** List of Social Media. */
  public ViewEventSocialMediaApiResponse getEventSocialMedia() {
    List<EventSocialMediaDao> eventCategories =
        (List<EventSocialMediaDao>) eventSocialMediaRepository.findAll();
    ViewEventSocialMediaApiResponse viewEventSocialMediaApiResponse =
        new ViewEventSocialMediaApiResponse();
    viewEventSocialMediaApiResponse.setEventSocialMedias(eventCategories);
    return viewEventSocialMediaApiResponse;
  }
}
