package id.myevent.model.apiresponse;

import java.util.List;
import lombok.Data;

/**
 * Event List Model.
 */
@Data
public class ViewEventListApiResponse {
  private List<EventData> eventDataList;
}
