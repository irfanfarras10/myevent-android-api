package id.myevent.model.apiresponse;

import id.myevent.model.dao.EventCategoryDao;
import java.util.List;
import lombok.Data;

/** View Event Category Api Response. */

@Data
public class ViewEventCategoryApiResponse {
  private List<EventCategoryDao> eventCategories;
}
