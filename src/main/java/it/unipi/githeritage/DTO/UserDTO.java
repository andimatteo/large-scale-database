package it.unipi.githeritage.DTO;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.unipi.githeritage.Model.MongoDB.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends BaseDTO{
    private String username;

    private String password;
    private String email;
    private String name;
    private String surname;
    private String nationality;
    private Integer followerNumber;
    private Integer followingNumber;
    private Boolean isAdmin;
    private Instant registrationDate;

    private List<String> commentIds;
    private List<String> projectIds;
    private List<String> commitIds;


    public static UserDTO fromUser(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setSurname(user.getSurname());
        userDTO.setFollowerNumber(user.getFollowerNumber());
        userDTO.setFollowingNumber(user.getFollowingNumber());
        userDTO.setProjectIds(user.getProjectIds());
        userDTO.setIsAdmin(user.getIsAdmin());
        return userDTO;        
    }
}
