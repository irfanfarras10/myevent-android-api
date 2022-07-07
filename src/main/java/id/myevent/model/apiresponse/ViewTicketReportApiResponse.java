package id.myevent.model.apiresponse;

import id.myevent.model.dao.TicketParticipantDao;
import java.util.List;
import lombok.Data;

/**
 * View Ticket Report Model.
 */
@Data
public class ViewTicketReportApiResponse {
  public List<TicketParticipantDao> ticketReportList;
}
