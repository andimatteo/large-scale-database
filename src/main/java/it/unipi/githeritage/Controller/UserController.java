package it.unipi.githeritage.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unipi.githeritage.DTO.ResponseDTO;
import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.Service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import it.unipi.githeritage.Config.Security.CustomUserDetails;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // PUT /api/user/user : update user info
    // query parameters: userDTO
    @PutMapping("/user")
    public ResponseEntity<ResponseDTO<UserDTO>> editPassword(@RequestBody UserDTO userDTO) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            String authenticatedUsername = authenticatedUser.getUsername();
            
            userDTO.setUsername(authenticatedUsername);
            userDTO.setIsAdmin(null); // Ensure isAdmin is not modified
            
            UserDTO updatedUser = userService.editUser(userDTO);

            return ResponseEntity.ok(new ResponseDTO<>(true, "Password updated successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating password: " + e.getMessage(), null));
        }
    }

    // DELETE /api/user/user : delete user
    // query parameters: username

    // POST /api/user/project : create new project
    // query parameters: projectDTO

    // PUT /api/user/project : update project
    // query parameters: projectDTO

    // DELETE /api/user/project : delete project
    // query parameters: projectDTO

    // PUT /api/user/project/ownership : change ownership of project
    // query parameters: new username

    // POST /api/user/commit : create new commit
    // query parameters: CommitDTO

    // PUT /api/user/commit : update commit
    // query parameters: CommitDTO

    // POST /api/user/file : create new file
    // query parameters: FileDTO with content

    // PUT /api/user/file : udpate file
    // query parameters: FileDTO with content

    // DELETE /api/user/file/{id} : delete file by id

    // DELETE /api/user/file : delete file by projectId and path
    // query parameters: projectId and path

    // GET /api/distance/follow/{username} : compute follow distance between me and user {username}

    // GET /api/distance/follow/{username}/{username} : compute follow distance between two users

    // GET /api/distance/project/{username} : compute project distance between me and user {username}

    // GET /api/distance/project/{username}/{username} : compute project distance between two users

    // GET /api/user/vulnerability : discover vulnerabilities on project {id} from graph
    // query parameters: project id

    // GET /api/user/discover/projects : discover new projects (based on projects of pepole i follow)
    // query parameters: username

    // GET /api/user/discover/people : discover new people I can follow (friends of friends)
    // query parameters: username

    // GET /api/user/inefficiencies : discover inefficiencies in project {id} from graph
    // query parameters: project id --> 20 metodi ordinati per hotness decrescente in un progetto
}
