package id.myevent.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.myevent.model.api_response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class GlobalUtil {
    //convert object to JSON
    public String convertObjectToJson(Object object) throws JsonProcessingException, JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
    //error handling for Spring Security Filter
    public void handleFilterError(int httpStatusCode, HttpStatus httpStatus, String message,
                                  HttpServletResponse response) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(httpStatusCode, httpStatus, message);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(convertObjectToJson(errorResponse));
    }
}
