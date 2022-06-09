package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.exception.ForbiddenException;
import id.myevent.model.dao.UserDao;
import id.myevent.model.dto.UserAuthDto;
import id.myevent.model.dto.UserDto;
import id.myevent.repository.UserRepository;
import id.myevent.util.GlobalUtil;
import id.myevent.util.JwtTokenUtil;
import java.util.ArrayList;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** User Service. */
@Service
@Slf4j
public class UserService implements UserDetailsService {
  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder bcryptEncoder;

  @Autowired private GlobalUtil globalUtil;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Override
  public UserAuthDto loadUserByUsername(String username) {
    UserDao user = userRepository.findByUsername(username);
    if (user == null) {
      throw new ForbiddenException("Username atau password salah");
    }
    return new UserAuthDto(
        user.getId(), 
        user.getUsername(), 
        user.getPassword(), 
        user.getOrganizerName(), 
        new ArrayList<>()
    );
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
      validateUserData(user);
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

  private String getUserId() {
    String tokenHeader = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest().getHeader("Authorization");

    String id = jwtTokenUtil.getSubjectFromToken(globalUtil.parseToken(tokenHeader));
    return id;
  }

  /**  View User Data. */
  public Optional<UserDao> getProfile() {
    return userRepository.findById(Long.parseLong(getUserId()));
  }

  /** Update User Data. */
  public void update(UserDto user) {

    Optional<UserDao> currentUser = userRepository.findById(Long.parseLong(getUserId()));
    UserDao newUser = currentUser.get();
    newUser.setEmail(user.getEmail());
    newUser.setPassword(user.getPassword());
    newUser.setOrganizerName(user.getOrganizerName());
    newUser.setPhoneNumber(user.getPhoneNumber());
    try {
      validateUserData(user);
      userRepository.save(newUser);
      // catch username or email value not unique
    } catch (DataIntegrityViolationException e) {
      String exceptionMessage = e.getMostSpecificCause().getMessage();
      String message = null;
      if (exceptionMessage.contains("email")) {
        message = "E-mail sudah digunakan";
      }
      throw new ConflictException(message);
    }
  }

  private void validateUserData(UserDto user) {
    if (!globalUtil.isEmail(user.getEmail())) {
      throw new ConflictException("Format e-mail tidak sesuai");
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
  }
}
