package id.myevent.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import id.myevent.model.notification.NotificationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  @Autowired
  FirebaseMessaging firebaseMessaging;

  public String sendNotification(NotificationData notificationData, String topic)
      throws FirebaseMessagingException {

    Notification notification = Notification
        .builder()
        .setTitle(notificationData.getSubject())
        .setBody(notificationData.getContent())
        .build();

    Message message = Message
        .builder()
        .setTopic(topic)
        .setNotification(notification)
        .putAllData(notificationData.getData())
        .build();

    return firebaseMessaging.send(message);
  }
}
