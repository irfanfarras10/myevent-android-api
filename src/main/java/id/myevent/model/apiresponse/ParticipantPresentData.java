package id.myevent.model.apiresponse;

import id.myevent.model.dao.TicketParticipantDao;
import lombok.Data;

/**
 * Present Data Model.
 */
@Data
public class ParticipantPresentData {
  private Long id;
  private String name;
  private String email;
  private String phoneNumber;
  private String status;
  private TicketParticipantDao ticketParticipants;
}
