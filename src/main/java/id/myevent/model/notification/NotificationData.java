package id.myevent.model.notification;

import java.util.Map;
import lombok.Data;

/**
 * Notification Model.
 */
@Data
public class NotificationData {
  private String subject;
  private String content;
  private Map<String, String> data;
  private String image;
}
