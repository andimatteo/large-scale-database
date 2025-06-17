package it.unipi.githeritage.Controller;

import java.time.Instant;
import java.util.List;

import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unipi.githeritage.Service.FileService;
import it.unipi.githeritage.Service.ProjectService;
import it.unipi.githeritage.Service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import it.unipi.githeritage.Config.Security.CustomUserDetails;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProjectService projectService;


    //////////////////////////////////////
    //         CRUD OPERATIONS          //
    //////////////////////////////////////

    //////////////////////////////////////
    //              USER                //
    //////////////////////////////////////

    // PUT /api/user/user : update user info
    // query parameters: userDTO
    @PutMapping("/user")
    public ResponseEntity<ResponseDTO<UserDTO>> editPassword(@RequestBody UserDTO userDTO) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
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
    @DeleteMapping("/user")
    public ResponseEntity<ResponseDTO<UserDTO>> deleteUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            String authenticatedUsername = authenticatedUser.getUsername();
            UserDTO dto = null;
            if (authenticatedUsername != null) {
                // delete user
                dto = userService.deleteUser(authenticatedUsername);
            }
            return ResponseEntity.ok(new ResponseDTO<>(true, "User deleted successfully", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error deleting user: " + e.getMessage(), null));
        }
    }

    //////////////////////////////////////
    //             PROJECT              //
    //////////////////////////////////////

    // POST /api/user/project : create new project
    // query parameters: projectDTO
    @PostMapping("/project")
    public ResponseEntity<ResponseDTO<?>> createProject(@RequestBody ProjectDTO projectDTO) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();

            // prevent setting of _id
            projectDTO.setId(null);

            // Set the username of the project to the authenticated user
            if (projectDTO.getOwner() == null) {
                projectDTO.setOwner(authenticatedUsername);
            }

            // set administrators
            if (projectDTO.getAdministrators() == null) {
                projectDTO.setAdministrators(List.of(authenticatedUsername));
            }

            // set version
            if (projectDTO.getVersion() == null) {
                projectDTO.setVersion("0");
            }

            // set creationDate
            if (projectDTO.getCreationDate() == null) {
                projectDTO.setCreationDate(Instant.now());
            }

            // Call the service to create the project
            ProjectDTO createdProject = projectService.addProject(projectDTO);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Project created successfully", createdProject));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error creating project: " + e.getMessage(), null));
        }
    }

    // PUT /api/user/project/{project-id} : update project
    // body: CommitIdDTO
    @PutMapping("/project/{project-id}")
    public ResponseEntity<ResponseDTO<?>> updateProject(
            @PathVariable String projectId, @RequestBody CommitIdDTO commitIdDTO
    ) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();

            String authenticatedUsername = authenticatedUser.getUsername();

            // Call the service to update the project
            Project updatedProject = projectService.updateProject(commitIdDTO, authenticatedUsername);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Project updated successfully", updatedProject));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
        }
    }

    // PUT /api/user/project/{project-id} : update project
    // body: CommitIdDTO
    @PutMapping("/project/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<?>> updateProject(
            @PathVariable String owner, @PathVariable String projectName, @RequestBody CommitOwnerDTO commitOwnerDTO
    ) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();

            String authenticatedUsername = authenticatedUser.getUsername();

            // Call the service to update the project
            ProjectDTO updatedProject = projectService.updateProject(commitOwnerDTO, authenticatedUsername);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Project updated successfully", updatedProject));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
        }
    }

    // DELETE /api/user/project?projectId=projectId : delete project
    // query parameters: projectId
//    @DeleteMapping("/project")
//    public ResponseEntity<ResponseDTO<ProjectDTO>> deleteProject(@RequestParam String projectId) {
//        try {
//            // Get the authenticated user from security context
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            //System.out.println("Authenticated user: " + authenticatedUser);
//            String authenticatedUsername = authenticatedUser.getUsername();
//            // Call the service to update the project
//            ProjectDTO deletedProject = projectService.deleteProject(projectId, authenticatedUser);
//            return ResponseEntity.ok(new ResponseDTO<>(true, "Project updated successfully", updatedProject));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
//        }
//    }

    // DELETE /api/user/project : delete project
    // query parameters: projectId
//    @DeleteMapping("/project/{owner}/{projectName}")
//    public ResponseEntity<ResponseDTO<ProjectDTO>> deleteProject(@PathVariable String owner,
//                                                        @PathVariable String projectName) {
//        try {
//            // Get the authenticated user from security context
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            //System.out.println("Authenticated user: " + authenticatedUser);
//            String authenticatedUsername = authenticatedUser.getUsername();
//            // Call the service to update the project
//            ProjectDTO deletedProject = projectService.deleteProject(owner, projectName, authenticatedUsername);
//            return ResponseEntity.ok(new ResponseDTO<>(true, "Project deleted successfully",
//                    deletedProject));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
//        }
//    }

    // NO
    // PUT /api/user/project/ownership : change ownership of project
    // query parameters: new username

    // NO
    // POST /api/user/commit : create new commit
    // query parameters: CommitDTO

    // NO
    // PUT /api/user/commit : update commit
    // query parameters: CommitDTO

    // NO
    // POST /api/user/file : create new file
    // query parameters: FileDTO with content

    // NO
//    @PostMapping("/file")
//    public ResponseEntity<ResponseDTO<?>> createFile(@RequestBody FileDTO fileDTO) {
//        try {
//            // Get the authenticated user from security context
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            //System.out.println("Authenticated user: " + authenticatedUser);
//            String authenticatedUsername = authenticatedUser.getUsername();
//
//            // Call the service to create the file
//            fileService.addFile(fileDTO, authenticatedUsername);
//            return ResponseEntity.ok(new ResponseDTO<>(true, "File created successfully", null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseDTO<>(false, "Error creating file: " + e.getMessage(), null));
//        }
//    }

    // NO
    // PUT /api/user/file : udpate file
    // query parameters: FileDTO with content
//    @PutMapping("/file")
//    public ResponseEntity<ResponseDTO<?>> updateFile(@RequestBody FileDTO fileDTO) {
//        try {
//            // Get the authenticated user from security context
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            String authenticatedUsername = authenticatedUser.getUsername();
//
//            // Call the service to update the file
//            fileService.updateFile(fileDTO, authenticatedUsername);
//            return ResponseEntity.ok(new ResponseDTO<>(true, "File updated successfully", null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseDTO<>(false, "Error updating file: " + e.getMessage(), null));
//        }
//    }

    // NO
//    // DELETE /api/user/file/{id} : delete file by id
//    @DeleteMapping("/file/{id}")
//    public ResponseEntity<ResponseDTO<?>> deleteFile(@PathVariable String id) {
//        try {
//            // Get the authenticated user from security context
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            String authenticatedUsername = authenticatedUser.getUsername();
//
//            // Call the service to delete the file
//            fileService.deleteFile(id, authenticatedUsername);
//            return ResponseEntity.ok(new ResponseDTO<>(true, "File deleted successfully", null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseDTO<>(false, "Error deleting file: " + e.getMessage(), null));
//        }
//    }

    // NO
//    // DELETE /api/user/file : delete file by projectId and path
//    // query parameters: projectId and path
//    @DeleteMapping("/file")
//    public ResponseEntity<ResponseDTO<?>> deleteFileByProjectAndPath(@RequestParam String projectId, @RequestParam String path) {
//        try {
//            // Get the authenticated user from security context
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
//            String authenticatedUsername = authenticatedUser.getUsername();
//
//            // Call the service to delete the file by projectId and path
//            fileService.deleteFile(projectId, path, authenticatedUsername);
//            return ResponseEntity.ok(new ResponseDTO<>(true, "File deleted successfully", null));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseDTO<>(false, "Error deleting file: " + e.getMessage(), null));
//        }
//    }


    // POST /api/user/follow : follow a user
    // query parameters: username to follow
    @PostMapping("/follow")
    public ResponseEntity<ResponseDTO<?>> followUser(@RequestParam String username) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();

            // Call the service to follow the user
            userService.followUser(authenticatedUsername, username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User followed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error following user: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/follow")
    public ResponseEntity<ResponseDTO<?>> unfollowUser(@RequestParam String username) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();
            // Call the service to unfollow the user
            userService.unfollowUser(authenticatedUsername, username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User unfollowed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error unfollowing user: " + e.getMessage(), null));
        }
    }

    // GET /api/distance/follow/{username} : compute follow distance between me and user {username}

    // GET /api/distance/follow/{username}/{username} : compute follow distance between two users

    // GET /api/distance/project/{username} : compute project distance between me and user {username}

    // GET /api/distance/project/{username}/{username} : compute project distance between two users

    // GET /api/user/vulnerability : discover vulnerabilities on project {id} from graph
    // query parameters: project id

    // GET /api/user/discover/projects : discover new projects (based on projects of pepole I follow)
    // query parameters: username

    // GET /api/user/discover/people : discover new people I can follow (friends of friends)
    // query parameters: username

    // GET /api/user/inefficiencies : discover inefficiencies in project {id} from graph
    // query parameters: project id --> 20 metodi ordinati per hotness decrescente in un progetto
}
