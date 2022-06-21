package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.exception.ForbiddenException;
import id.myevent.model.dao.EventOrganizerDao;
import id.myevent.model.dto.EventOrganizerAuthDto;
import id.myevent.model.dto.EventOrganizerDto;
import id.myevent.repository.EventOrganizerRepository;
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
public class EventOrganizerService implements UserDetailsService {
  @Autowired private EventOrganizerRepository eventOrganizerRepository;

  @Autowired private PasswordEncoder bcryptEncoder;

  @Autowired private GlobalUtil globalUtil;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Override
  public EventOrganizerAuthDto loadUserByUsername(String username) {
    EventOrganizerDao user = eventOrganizerRepository.findByUsername(username);
    if (user == null) {
      throw new ForbiddenException("Username atau password salah");
    }
    return new EventOrganizerAuthDto(
        user.getId(), user.getUsername(), user.getPassword(), new ArrayList<>());
  }

  /** Insert User Data to Database. */
  public void insert(EventOrganizerDto user) {
    EventOrganizerDao newUser = new EventOrganizerDao();
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
      validateUserDataForSignUp(user);
      eventOrganizerRepository.save(newUser);
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
    String tokenHeader =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest()
            .getHeader("Authorization");

    String id = jwtTokenUtil.getSubjectFromToken(globalUtil.parseToken(tokenHeader));
    return id;
  }

  /** View User Data. */
  public Optional<EventOrganizerDao> getProfile() {
    return eventOrganizerRepository.findById(Long.parseLong(getUserId()));
  }

  /** Update User Data. */
  public void update(EventOrganizerDto user) {

    Optional<EventOrganizerDao> currentUser =
        eventOrganizerRepository.findById(Long.parseLong(getUserId()));
    EventOrganizerDao newUser = currentUser.get();
    newUser.setEmail(user.getEmail());
    newUser.setOrganizerName(user.getOrganizerName());
    newUser.setPhoneNumber(user.getPhoneNumber());
    try {
      validateUserDataForUpdate(user);
      eventOrganizerRepository.save(newUser);
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

  private void validateUserDataForSignUp(EventOrganizerDto user) {
    if (globalUtil.isBlankString(user.getUsername())) {
      throw new ConflictException("Username harus diisi");
    }
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

  private void validateUserDataForUpdate(EventOrganizerDto user) {
    if (!globalUtil.isEmail(user.getEmail())) {
      throw new ConflictException("Format e-mail tidak sesuai");
    }
    if (globalUtil.isBlankString(user.getEmail())) {
      throw new ConflictException("E-mail harus diisi");
    }
    if (globalUtil.isBlankString(user.getOrganizerName())) {
      throw new ConflictException("Nama Event Organizer harus diisi");
    }
    if (globalUtil.isBlankString(user.getPhoneNumber())) {
      throw new ConflictException("Nomor telepon harus diisi");
    }
  }
}
