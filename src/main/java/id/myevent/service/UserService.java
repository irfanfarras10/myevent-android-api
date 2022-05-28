package id.myevent.service;

import id.myevent.exception.ConflictException;
import id.myevent.model.DAO.UserDAO;
import id.myevent.model.DTO.UserDTO;
import id.myevent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDAO user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User tidak ditemukan");
        }
        return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    public UserDAO insert(UserDTO user){
        UserDAO newUser = new UserDAO();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        newUser.setOrganizerName(user.getOrganizerName());
        newUser.setPhoneNumber(user.getPhoneNumber());
        return userRepository.save(newUser);
    }
}
