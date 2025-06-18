package it.unipi.githeritage.Model.MongoDB;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unipi.githeritage.DTO.UserDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Users")
public class User {

    @Id
    @JsonProperty("_id")
    private String username;
    private String password;
    private String email;
    private String name;
    private String surname;
    private String nationality;
    private Integer followerNumber;
    private Integer followingNumber;
    private List<String> comments;
    private List<String> projects;
    private Boolean isAdmin;
    private Instant registrationDate;

    public static User fromDTO(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setNationality(userDTO.getNationality());
        user.setFollowerNumber(userDTO.getFollowerNumber());
        user.setFollowingNumber(userDTO.getFollowingNumber());
        user.setProjects(userDTO.getProjects());
        user.setIsAdmin(userDTO.getIsAdmin());
        return user;
    }
}
