package id.myevent.model.apiresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;


/** Error Response Model. */
@AllArgsConstructor
@Data
public class ErrorResponse {
  private int httpStatusCode;
  private HttpStatus httpStatus;
  private String message;
}
