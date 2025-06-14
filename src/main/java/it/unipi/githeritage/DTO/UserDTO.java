package it.unipi.githeritage.DTO;

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
    private List<String> comments;
    private List<String> projects;
    private Boolean isAdmin;
    //@JsonFormat(pattern = "yyyy-MM-dd")
    //private LocalDate registrationDate;


    public static UserDTO fromUser(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
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
