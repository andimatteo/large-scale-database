package it.unipi.githeritage.Controller;

import it.unipi.githeritage.Config.Security.CustomUserDetails;
import it.unipi.githeritage.DTO.ResponseDTO;
import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.DTO.UserMetadataDTO;
import it.unipi.githeritage.Service.ProjectService;
import it.unipi.githeritage.Service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final ProjectService projectService;

    // PUT /api/admin/superuser/{username} : make another user Admin
    @PutMapping("/superuser/{username}")
    public ResponseEntity<ResponseDTO> superUser(@PathVariable String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();

            String authenticatedUsername = authenticatedUser.getUsername();
            Boolean isAdmin = authenticatedUser.getIsAdmin();

            if (authenticatedUsername == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "Not logged in", null));
            }

            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "You are not Admin", null));
            }

            Long code = userService.superUser(username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User deleted successfully", code));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error deleting user: " + e.getMessage(), null));
        }
    }

    // DELETE /api/admin/superuser/{username} : remove Admin role to user
    @DeleteMapping("/superuser/{username}")
    public ResponseEntity<ResponseDTO> removeSuperUser(@PathVariable String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();

            String authenticatedUsername = authenticatedUser.getUsername();
            Boolean isAdmin = authenticatedUser.getIsAdmin();

            if (authenticatedUsername == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "Not logged in", null));
            }

            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "You are not Admin", null));
            }

            Long code = userService.removeSuperUser(username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User deleted successfully", code));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error deleting user: " + e.getMessage(), null));
        }
    }

    // DELETE /api/admin/user/{username} : delete arbitrary user
    @DeleteMapping("/user/{username}")
    public ResponseEntity<ResponseDTO<Long>> deleteUser(
            @PathVariable String username
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            String authenticatedUsername = authenticatedUser.getUsername();

            Boolean isAdmin = authenticatedUser.getIsAdmin();
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "You are not Admin", null));
            }

            Long code = null;
            if (authenticatedUsername != null) {
                code = userService.deleteUser(username);
            }
            return ResponseEntity.ok(new ResponseDTO<>(true, "User deleted successfully", code));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error deleting user: " + e.getMessage(), null));
        }
    }

    // GET /api/admin/user/all : get list of all users (max 200)
    @GetMapping("/user/all")
    public ResponseEntity<ResponseDTO<List<UserMetadataDTO>>> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            String authenticatedUsername = authenticatedUser.getUsername();

            Boolean isAdmin = authenticatedUser.getIsAdmin();
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "You are not Admin", null));
            }

            List<UserMetadataDTO> dto = userService.getAllUserMetadata();
            return ResponseEntity.ok(new ResponseDTO<>(true, "First 200 users", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error getting user: " + e.getMessage(), null));
        }
    }

    // GET /api/admin/user/all/{page} : get list of all users (paginated)
    @GetMapping("/user/all/{page}")
    public ResponseEntity<ResponseDTO<List<UserMetadataDTO>>> getAllUsersPaginated(@PathVariable int page) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            String authenticatedUsername = authenticatedUser.getUsername();

            Boolean isAdmin = authenticatedUser.getIsAdmin();
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "You are not Admin", null));
            }

            List<UserMetadataDTO> dto = userService.getAllUsersMetadataPaginated(page);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Users for page " + page, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error deleting user: " + e.getMessage(), null));
        }
    }

    // PUT /api/admin/collaborators/{owner}/{projectName}/{username} : change permission on every project
    @PutMapping("/collaborators/{owner}/{projectName}/{username}")
    public ResponseEntity<ResponseDTO<?>> updateCollaborator(
            @PathVariable String owner,
            @PathVariable String projectName,
            @PathVariable String username
    ) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();

            String authenticatedUsername = authenticatedUser.getUsername();
            Boolean isAdmin = authenticatedUser.getIsAdmin();

            if (authenticatedUsername == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO<>(false, "Not logged in", null));
            }

            // Call the service to update the project
            projectService.updateCollaborators(owner, projectName, authenticatedUsername, username, null, isAdmin);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Successfully added collaborator", username));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
        }
    }
}
