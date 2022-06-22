package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventSocialMediaDao;
import java.util.List;
import lombok.Data;

/** Model View Event Social Media.*/
@Data
public class ViewEventSocialMediaApiResponse {
  private List<EventSocialMediaDao> eventSocialMedias;
}
