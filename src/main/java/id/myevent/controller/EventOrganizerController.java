package id.myevent.controller;

import id.myevent.exception.ForbiddenException;
import id.myevent.exception.UnauthorizedException;
import id.myevent.model.apirequest.SignInApiRequest;
import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.SignInApiResponse;
import id.myevent.model.dao.EventOrganizerDao;
import id.myevent.model.dto.EventOrganizerAuthDto;
import id.myevent.model.dto.EventOrganizerDto;
import id.myevent.service.EventOrganizerService;
import id.myevent.util.JwtTokenUtil;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** User REST Controller. */
@CrossOrigin
@RestController
@RequestMapping("/api")
@Slf4j
public class EventOrganizerController {
  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired private EventOrganizerService eventOrganizerService;

  private void authenticate(String username, String password) throws UnauthorizedException {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));
    } catch (DisabledException e) {
      throw new ForbiddenException("User dinonaktifkan");
    } catch (BadCredentialsException e) {
      throw new ForbiddenException("Username atau password salah");
    }
  }

  /** Sign In Endpoint. */
  @PostMapping("/auth/signin")
  public ResponseEntity<SignInApiResponse> signIn(@RequestBody SignInApiRequest signInApiRequest)
      throws Exception {

    authenticate(signInApiRequest.getUsername(), signInApiRequest.getPassword());

    final EventOrganizerAuthDto eventOrganizerAuthDto =
        eventOrganizerService.loadUserByUsername(signInApiRequest.getUsername());

    final String token = jwtTokenUtil.generateToken(eventOrganizerAuthDto);

    return ResponseEntity.ok(
        new SignInApiResponse(token, eventOrganizerAuthDto.getOrganizerName()));
  }

  /** Sign Up Endpoint. */
  @PostMapping("/auth/signup")
  public ResponseEntity<ApiResponse> signUp(@RequestBody EventOrganizerDto signUpApiRequest) {
    eventOrganizerService.insert(signUpApiRequest);
    return new ResponseEntity<ApiResponse>(
        new ApiResponse("Registrasi Berhasil"), HttpStatus.CREATED);
  }

  @GetMapping("/hello")
  public String helloWorld() {
    return "Hello World";
  }

  /** View Profile Endpoint. */
  @GetMapping("/users/profile")
  public Optional<EventOrganizerDao> viewProfile() {
    return eventOrganizerService.getProfile();
  }

  /** Edit Profile Endpoint. */
  @PutMapping("/users/profile")
  public ResponseEntity<ApiResponse> editProfile(@RequestBody EventOrganizerDto user) {
    eventOrganizerService.update(user);
    return ResponseEntity.ok(new ApiResponse("Profil Berhasil di Update"));
  }
}
