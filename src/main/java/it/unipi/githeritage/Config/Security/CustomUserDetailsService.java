package it.unipi.githeritage.Config.Security;

import org.springframework.security.core.userdetails.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.Service.UserService;
import lombok.AllArgsConstructor;

// Simulate fetching from DB
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private final UserService userService;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDTO user;
        try {
            user = userService.getUserByUsername(username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }

        return new CustomUserDetails(user);
    }
}
