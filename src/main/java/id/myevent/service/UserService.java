package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.dao.UserDao;
import id.myevent.model.dto.UserDto;
import id.myevent.repository.UserRepository;
import id.myevent.util.GlobalUtil;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** User Service. */
@Service
@Slf4j
public class UserService implements UserDetailsService {
  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder bcryptEncoder;

  @Autowired private GlobalUtil globalUtil;

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
    // validate null or empty string
    try {
      if (globalUtil.isBlankString(user.getUsername())) {
        throw new ConflictException("Username harus diisi");
      }
      if (globalUtil.isBlankString(user.getEmail())) {
        throw new ConflictException("E-mail harus diisi");
      }
      if (globalUtil.isBlankString(user.getPassword())) {
        throw new ConflictException("Password harus diisi");
      }
      if (globalUtil.isBlankString(user.getOrganizerName())) {
        throw new ConflictException("Nama Event Organizer harus diisi");
      }
      if (globalUtil.isBlankString(user.getPhoneNumber())) {
        throw new ConflictException("Nomor telepon harus diisi");
      }
      return userRepository.save(newUser);
      // catch username or email value not unique
    } catch (DataIntegrityViolationException e) {
      String exceptionMessage = e.getMostSpecificCause().getMessage();
      String message = null;
      if (exceptionMessage.contains("username")) {
        message = "Username sudah digunakan";
      } else if (exceptionMessage.contains("email")) {
        message = "E-mail sudah digunakan";
      }
      throw new ConflictException(message);
    }
  }
}
