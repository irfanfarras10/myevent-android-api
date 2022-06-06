package id.myevent.controller;

import id.myevent.exception.UnauthorizedException;
import id.myevent.model.apirequest.SignInApiRequest;
import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.SignInApiResponse;
import id.myevent.model.dao.UserDao;
import id.myevent.model.dto.UserAuthDto;
import id.myevent.model.dto.UserDto;
import id.myevent.service.UserService;
import id.myevent.util.JwtTokenUtil;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserController {
  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired private UserService userService;

  private void authenticate(String username, String password) throws UnauthorizedException {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));
    } catch (DisabledException e) {
      throw new UnauthorizedException("User dinonaktifkan");
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException("Username atau password salah");
    }
  }

  /** Sign In Endpoint. */
  @PostMapping("/auth/signin")
  public ResponseEntity<SignInApiResponse> signIn(@RequestBody SignInApiRequest signInApiRequest)
      throws Exception {

    authenticate(signInApiRequest.getUsername(), signInApiRequest.getPassword());

    final UserAuthDto userAuthDto = userService.loadUserByUsername(signInApiRequest.getUsername());

    final String token = jwtTokenUtil.generateToken(userAuthDto);

    return ResponseEntity.ok(new SignInApiResponse(token));
  }

  /** Sign Up Endpoint. */
  @PostMapping("/auth/signup")
  public ResponseEntity<ApiResponse> signUp(@RequestBody UserDto signUpApiRequest) {
    userService.insert(signUpApiRequest);
    return ResponseEntity.ok(new ApiResponse("Registrasi Berhasil"));
  }

  @GetMapping("/hello")
  public String helloWorld() {
    return "Hello World";
  }

  /** View Profile Endpoint. */
  @GetMapping("/users/profile")
  public Optional<UserDao> viewProfile() {
    return userService.getProfile();
  }

  /** Edit Profile Endpoint. */
  @PutMapping("/users/profile")
  public ResponseEntity<ApiResponse> editProfile(@RequestBody UserDto user) {
    userService.update(user);
    return ResponseEntity.ok(new ApiResponse("Profil Berhasil di Update"));
  }

}
