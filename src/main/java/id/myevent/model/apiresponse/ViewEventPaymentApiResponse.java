package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventPaymentDao;
import java.util.List;
import lombok.Data;

/**
 * View Payment Model.
 */
@Data
public class ViewEventPaymentApiResponse {
  private List<EventPaymentDao> eventPayments;
}
