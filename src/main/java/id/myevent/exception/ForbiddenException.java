package id.myevent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** HTTP Error Forbidden Exception. */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
  private String message;

  public ForbiddenException(String message) {
    super(message);
  }
}
