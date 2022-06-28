package id.myevent.task;

import id.myevent.model.dao.EventDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reminder Day Min One Event Task.
 */
@Slf4j
@Component
public class ReminderOneEventTask implements Runnable {

  public EventDao getEvent() {
    return event;
  }

  public void setEvent(EventDao event) {
    this.event = event;
  }

  EventDao event;

  @Override
  public void run() {
    log.warn("notifikasi day min one untuk event dengan id " + event.getId());
  }
}
