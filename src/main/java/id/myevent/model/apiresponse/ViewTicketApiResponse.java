package id.myevent.model.apiresponse;

import id.myevent.model.dao.TicketDao;
import java.util.List;
import lombok.Data;

/**
 * View Ticket Model.
 */
@Data
public class ViewTicketApiResponse {
  List<TicketDao> ticketList;
}
