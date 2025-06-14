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

    // create an endpoint to edit user parameters
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
    
    // GET /api/user/vulnerability : discover vulnerabilities on project {id} from graph
    // query parameters: project id

    // GET /api/user/inefficiencies : discover inefficiencies in project {id} from graph
    // query parameters: project id

    // GET /
}
