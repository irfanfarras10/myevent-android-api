package id.myevent.model.apiresponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Sign In API Response. */
@Setter
@Getter
@AllArgsConstructor
public class SignInApiResponse {
  private String token;
  private String organizerName;
}
