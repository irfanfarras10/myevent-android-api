package id.myevent.task;

import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventStatusDao;
import id.myevent.repository.EventRepository;
import id.myevent.repository.EventStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Passed Event Task.
 */
@Slf4j
@Component
public class PassedEventTask implements Runnable {

  @Autowired
  EventRepository eventRepository;

  @Autowired
  EventStatusRepository eventStatusRepository;

  public EventDao getEvent() {
    return event;
  }

  public void setEvent(EventDao event) {
    this.event = event;
  }

  EventDao event;

  @Override
  public void run() {
    //update event status to passed

    final EventStatusDao passedEventStatus = eventStatusRepository.findById(4L).get();
    event.setEventStatus(passedEventStatus);
    eventRepository.save(event);
    log.warn("update status menjadi passed untuk event dengan id " + event.getId());
  }
}
