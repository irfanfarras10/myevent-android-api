package id.myevent.model.apiresponse;

import java.util.List;
import lombok.Data;

/**
 * View Event Agenda Response.
 */
@Data
public class ViewEventAgendaApiResponse {
  private List<AgendaEventData> agendaEventDataList;
}
