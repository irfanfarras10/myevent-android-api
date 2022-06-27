package id.myevent.model.notification;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationData {
    private String subject;
    private String content;
    private Map<String, String> data;
    private String image;
}
