package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventGuestDao;
import java.util.List;
import lombok.Data;

/**
 * Guest List Response.
 */
@Data
public class ViewEventGuestListApiResponse {
  private List<EventGuestDao> listGuest;
}
