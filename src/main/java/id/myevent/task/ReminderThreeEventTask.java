package id.myevent.task;

import com.google.firebase.messaging.FirebaseMessagingException;
import id.myevent.model.dao.EventDao;
import id.myevent.model.notification.NotificationData;
import id.myevent.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Reminder Day Min Three Event Task.
 */
@Slf4j
@Component
public class ReminderThreeEventTask implements Runnable {
  @Autowired
  NotificationService notificationService;

  public EventDao getEvent() {
    return event;
  }

  public void setEvent(EventDao event) {
    this.event = event;
  }

  EventDao event;

  @Override
  public void run() {
    NotificationData notification = new NotificationData();
    Map<String, String> notificationData = new HashMap<>();
    notificationData.put("eventId", String.valueOf(event.getId()));
    notification.setSubject("Event " + event.getName() + " sebentar lagi akan dilaksanakan");
    notification.setContent("Jangan lewatkan event " + event.getName() + " yang akan dilaksanakan sebentar lagi");
    notification.setData(notificationData);
    try {
      notificationService.sendNotification(notification, String.valueOf(event.getEventOrganizer().getId()));
    } catch (FirebaseMessagingException e) {
      throw new RuntimeException(e);
    }
  }
}
