package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventStatusDao;
import lombok.Data;

/**
 * Event Agenda Model.
 */
@Data
public class AgendaEventData {
  private String name;
  private Long dateTimeEventStart;
  private Long dateTimeEventEnd;
  private EventStatusDao eventStatus;
}
