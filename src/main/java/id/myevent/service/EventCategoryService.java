package id.myevent.service;

import id.myevent.model.dao.EventCategoryDao;
import id.myevent.repository.EventCategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Event Category Service.
 */
@Service
public class EventCategoryService {

  @Autowired
  EventCategoryRepository eventCategoryRepository;

  public List<EventCategoryDao> getEventCategory() {
    return (List<EventCategoryDao>) eventCategoryRepository.findAll();
  }
}
