package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventPaymentDao;
import id.myevent.model.dao.TicketDao;
import lombok.Data;

/**
 * Partipant Model.
 */
@Data
public class ViewEventParticipantApiResponse {
  private Long id;
  private String name;
  private String email;
  private String phoneNumber;
  private String ticket;
  private String paymentProofPhoto;
}
