package id.myevent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** HTTP Error Unauthorized Exception. */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
  private String message;

  public UnauthorizedException(String message) {
    super(message);
  }
}
