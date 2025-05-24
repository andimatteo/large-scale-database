package it.unipi.githeritage.model.mongodb;

import org.springframework.data.annotation.Id;
import it.unipi.githeritage.utils.Role;

public class User {
    @Id
    private final String id;

    private final String username;
    private final String password;
    private Role role;

    public User(Role role, String username, String password, String id) {
        this.role = role;
        this.username = username;
        this.password = password;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
