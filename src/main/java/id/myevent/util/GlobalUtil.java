package id.myevent.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.myevent.model.apiresponse.ErrorResponse;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Global Util That Contain Common Function. */
@Component
public class GlobalUtil {
  /** Convert Object to JSON. */
  public String convertObjectToJson(Object object)
      throws JsonProcessingException, JsonProcessingException {
    if (object == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(object);
  }

  /** Error Handler for Spring Security. */
  public void handleFilterError(
      int httpStatusCode, HttpStatus httpStatus, String message, HttpServletResponse response)
      throws IOException {
    ErrorResponse errorResponse = new ErrorResponse(httpStatusCode, httpStatus, message);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.getWriter().write(convertObjectToJson(errorResponse));
  }

  public Boolean isBlankString(String string) {
    return string == null || string.trim().isEmpty();
  }

  /** E-mail validation. */
  public Boolean isEmail(String email) {
    String emailRegexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    if (email.matches(emailRegexPattern)) {
      return true;
    }
    return false;
  }
}
