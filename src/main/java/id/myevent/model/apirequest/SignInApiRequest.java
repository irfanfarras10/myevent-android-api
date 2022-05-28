package id.myevent.model.apirequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Sign In API Request Model. */
@Setter
@Getter
@AllArgsConstructor
public class SignInApiRequest {
  private String username;
  private String password;
}
