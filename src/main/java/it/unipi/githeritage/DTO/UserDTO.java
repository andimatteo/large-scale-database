package it.unipi.githeritage.DTO;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unipi.githeritage.model.mongodb.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends BaseDTO{
    private String username;
    private String passwordHash;
    private String email;
    private String name;
    private String surname;
    private String nationality;
    private Integer followerNumber;
    private Integer followingNumber;
    private List<String> comments;
    private List<String> projects;
    private Boolean isAdmin;
    private String authorId; // Add this field for security integration
    //@JsonFormat(pattern = "yyyy-MM-dd")
    //private LocalDate registrationDate;


    public static UserDTO fromUser(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setPasswordHash(user.getPasswordHash());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setSurname(user.getSurname());
        userDTO.setNationality(user.getNationality());
        userDTO.setFollowerNumber(user.getFollowerNumber());
        userDTO.setFollowingNumber(user.getFollowingNumber());
        userDTO.setComments(user.getComments());
        userDTO.setProjects(user.getProjects());
        userDTO.setIsAdmin(user.getIsAdmin());
        return userDTO;        
    }
}
