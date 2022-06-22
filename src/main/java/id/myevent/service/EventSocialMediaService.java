package id.myevent.service;

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

  public List<EventSocialMediaDao> getEventSocialMedia() {
    return (List<EventSocialMediaDao>) eventSocialMediaRepository.findAll();
  }
}
