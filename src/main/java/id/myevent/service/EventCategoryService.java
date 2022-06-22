package id.myevent.service;

import com.sun.glass.events.ViewEvent;
import id.myevent.model.apiresponse.ViewEventCategoryApiResponse;
import id.myevent.model.dao.EventCategoryDao;
import id.myevent.repository.EventCategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Event Category Service. */
@Service
public class EventCategoryService {

  @Autowired EventCategoryRepository eventCategoryRepository;

  /** View Event Categories. */
  public ViewEventCategoryApiResponse getEventCategories() {
    List<EventCategoryDao> eventCategories =
        (List<EventCategoryDao>) eventCategoryRepository.findAll();
    ViewEventCategoryApiResponse viewEventCategoryApiResponse = new ViewEventCategoryApiResponse();
    viewEventCategoryApiResponse.setEventCategories(eventCategories);
    return viewEventCategoryApiResponse;
  }
}
