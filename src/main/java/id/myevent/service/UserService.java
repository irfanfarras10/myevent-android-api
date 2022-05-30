package id.myevent.service;

import id.myevent.model.dao.UserDao;
import id.myevent.model.dto.UserDto;
import id.myevent.repository.UserRepository;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** User Service. */
@Service
public class UserService implements UserDetailsService {
  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder bcryptEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserDao user = userRepository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User tidak ditemukan");
    }
    return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
  }

  /** Insert User Data to Database. */
  public UserDao insert(UserDto user) {
    UserDao newUser = new UserDao();
    newUser.setUsername(user.getUsername());
    newUser.setEmail(user.getEmail());
    newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
    newUser.setOrganizerName(user.getOrganizerName());
    newUser.setPhoneNumber(user.getPhoneNumber());
    return userRepository.save(newUser);
  }
}
