package id.myevent.model.apiresponse;

import id.myevent.model.dao.ParticipantDao;
import java.util.List;
import lombok.Data;

/**
 * List Participant Model.
 */
@Data
public class ViewEventParticipantListApiResponse {
  private List<ParticipantDao> listParticipant;
}
