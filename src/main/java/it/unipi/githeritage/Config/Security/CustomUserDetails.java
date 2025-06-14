package it.unipi.githeritage.Config.Security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import it.unipi.githeritage.DTO.UserDTO; // Import UserDTO
import lombok.Data;

import java.util.List; // Import List
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import SimpleGrantedAuthority

import java.util.Collection;

@Data
public class CustomUserDetails implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final Boolean isAdmin;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String id, String username, String password, Boolean isAdmin, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.authorities = authorities;
    }

    public CustomUserDetails(UserDTO userDTO) {
        this.id = userDTO.getUsername();
        this.username = userDTO.getUsername();
        this.password = userDTO.getPassword();
        this.isAdmin = userDTO.getIsAdmin();
        // Map isAdmin boolean to role string
        String role = userDTO.getIsAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
