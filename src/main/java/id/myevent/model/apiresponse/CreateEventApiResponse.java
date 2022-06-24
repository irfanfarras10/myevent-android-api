package id.myevent.model.apiresponse;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Create Event Api Respons Model. */
@Data
@AllArgsConstructor
public class CreateEventApiResponse {
  private String message;
  private long eventId;
}
