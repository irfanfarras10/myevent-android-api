package id.myevent.model.api_response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Data
public class ErrorResponse {
    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String message;
}
