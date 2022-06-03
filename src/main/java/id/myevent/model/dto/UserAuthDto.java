package id.myevent.model.dto;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Custom UserDetails for Auth. */
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

  public Long getId() {
    return id;
  }
}
