package id.myevent.controller;

import id.myevent.exception.ConflictException;
import id.myevent.model.DAO.UserDAO;
import id.myevent.model.DTO.UserDTO;
import id.myevent.model.api_request.SignInApiRequest;
import id.myevent.model.api_response.ApiResponse;
import id.myevent.model.api_response.SignInApiResponse;
import id.myevent.repository.UserRepository;
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

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;


    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<SignInApiResponse> signIn(@RequestBody SignInApiRequest signInApiRequest) throws Exception {

        authenticate(signInApiRequest.getUsername(), signInApiRequest.getPassword());

        final UserDetails userDetails = userService.loadUserByUsername(signInApiRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new SignInApiResponse(token));
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse> signUp(@RequestBody UserDTO signUpApiRequest) throws Exception{
        try{
            UserDAO registerUser = userService.insert(signUpApiRequest);
        }catch (Exception e){
            throw new ConflictException("Registrasi Gagal", e);
        }

        return ResponseEntity.ok(new ApiResponse("Registrasi Berhasil"));
    }
}
