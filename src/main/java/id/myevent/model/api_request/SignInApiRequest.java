package id.myevent.model.api_request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SignInApiRequest {
    private String username;
    private String password;
}
