package id.myevent.controller;

import id.myevent.model.apirequest.SignInApiRequest;
import id.myevent.model.apiresponse.ApiResponse;
import id.myevent.model.apiresponse.SignInApiResponse;
import id.myevent.model.dao.UserDao;
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
import org.springframework.web.bind.annotation.*;

/** User REST Controller. */
@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserController {
  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired private UserService userService;

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
    userService.insert(signUpApiRequest);
    return ResponseEntity.ok(new ApiResponse("Registrasi Berhasil"));
  }

  /** View Profile Endpoint. */
  @GetMapping("/users/{username}")
  public UserDao viewProfile(@PathVariable("username") String username){
    return userService.getProfile(username);
  }
}
