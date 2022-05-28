package id.myevent.controller;

import id.myevent.exception.ConflictException;
import id.myevent.model.apirequest.SignInApiRequest;
import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.SignInApiResponse;
import id.myevent.model.dto.UserDto;
import id.myevent.service.UserService;
import id.myevent.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** User REST Controller. */
@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserController {
  private final AuthenticationManager authenticationManager;

  private final JwtTokenUtil jwtTokenUtil;

  private final UserService userService;

  /** Autowiring by constructor. */
  @Autowired
  public UserController(
      AuthenticationManager authenticationManager,
      JwtTokenUtil jwtTokenUtil,
      UserService userService) {
    this.authenticationManager = authenticationManager;
    this.jwtTokenUtil = jwtTokenUtil;
    this.userService = userService;
  }

  private void authenticate(String username, String password) throws Exception {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));
    } catch (DisabledException e) {
      throw new Exception("USER_DISABLED", e);
    } catch (BadCredentialsException e) {
      throw new Exception("INVALID_CREDENTIALS", e);
    }
  }

  @GetMapping("/hello")
  public String hello() {
    return "Hello World";
  }

  /** Sign In Endpoint. */
  @PostMapping("/auth/signin")
  public ResponseEntity<SignInApiResponse> signIn(@RequestBody SignInApiRequest signInApiRequest)
      throws Exception {

    authenticate(signInApiRequest.getUsername(), signInApiRequest.getPassword());

    final UserDetails userDetails = userService.loadUserByUsername(signInApiRequest.getUsername());

    final String token = jwtTokenUtil.generateToken(userDetails);

    return ResponseEntity.ok(new SignInApiResponse(token));
  }

  /** Sign Up Endpoint. */
  @PostMapping("/auth/signup")
  public ResponseEntity<ApiResponse> signUp(@RequestBody UserDto signUpApiRequest) {
    try {
      userService.insert(signUpApiRequest);
    } catch (Exception e) {
      throw new ConflictException("Registrasi Gagal", e);
    }

    return ResponseEntity.ok(new ApiResponse("Registrasi Berhasil"));
  }
}