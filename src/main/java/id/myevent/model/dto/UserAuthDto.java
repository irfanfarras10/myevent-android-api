package id.myevent.model.dto;

import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Custom UserDetails for Auth. */
@Setter
@Getter
public class UserAuthDto extends User {
  private Long id;

  public UserAuthDto(
      Long id,
      String username,
      String password,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
    this.id = id;
  }
}
