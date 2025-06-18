package it.unipi.githeritage.Controller;

import java.time.Instant;
import java.util.List;

import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Project;
import it.unipi.githeritage.Model.Neo4j.Method;
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

            // todo if not logged then raise error
            
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
            // todo if not logged then raise error
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


    // todo: quando modifico file e creo methods vanno aggiornati tutti i methods verso cui creo un arco e cosi' via...
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

            // todo if not logged then raise error

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

    // todo: quando modifico file e creo methods vanno aggiornati tutti i methods verso cui creo un arco e cosi' via...
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
            // todo if not logged then raise error

            // Call the service to update the project
            Project updatedProject = projectService.updateProject(commitIdDTO, authenticatedUsername);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Project updated successfully", updatedProject));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
        }
    }

    // todo: quando modifico file e creo methods vanno aggiornati tutti i methods verso cui creo un arco e cosi' via...
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
            // todo if not logged then raise error

            // Call the service to update the project
            ProjectDTO updatedProject = projectService.updateProject(commitOwnerDTO, authenticatedUsername);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Project updated successfully", updatedProject));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
        }
    }

    // todo: quando modifico file e creo methods vanno aggiornati tutti i methods verso cui creo un arco e cosi' via...
    // DELETE /api/user/project?projectId=projectId : delete project
    // query parameters: projectId
    @DeleteMapping("/project")
    public ResponseEntity<ResponseDTO<ProjectDTO>> deleteProject(@RequestParam String projectId) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String username = authenticatedUser.getUsername();
            // todo if not logged then raise error

            // Call the service to update the project
            ProjectDTO deletedProject = projectService.deleteProject(projectId, username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Project updated successfully", deletedProject));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
        }
    }

    // todo: quando modifico file e creo methods vanno aggiornati tutti i methods verso cui creo un arco e cosi' via...
    // DELETE /api/user/project : delete project
    // query parameters: projectId
    @DeleteMapping("/project/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<ProjectDTO>> deleteProject(@PathVariable String owner,
                                                        @PathVariable String projectName) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();
            // todo if not logged then raise error

            // Call the service to update the project
            ProjectDTO deletedProject = projectService.deleteProject(owner, projectName, authenticatedUsername);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Project deleted successfully",
                    deletedProject));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating project: " + e.getMessage(), null));
        }
    }

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
            // todo if not logged then raise error

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
            // todo if not logged then raise error

            // Call the service to unfollow the user
            userService.unfollowUser(authenticatedUsername, username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User unfollowed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error unfollowing user: " + e.getMessage(), null));
        }
    }

    // GET /api/distance/follow/{username} : compute follow distance between me and user {username}
    @GetMapping("/distance/follow/{username}")
    public ResponseEntity<ResponseDTO<PathDTO>> getFollowPath(@PathVariable String username) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();
            // todo if not logged then raise error

            PathDTO pathDTO = userService.getFollowPath(authenticatedUsername, username);
            String msg = pathDTO.getDistance() < 0
                    ? "No vulnerable dependencies found"
                    : "Vulnerability path retrieved";
            return ResponseEntity.ok(new ResponseDTO<>(true, msg, pathDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding distance: " + e.getMessage(), null));
        }
    }

    // GET /api/distance/follow/{username}/{username} : compute follow distance between two users
    @GetMapping("/distance/follow/{username1}/{username2}")
    public ResponseEntity<ResponseDTO<PathDTO>> getFollowPath(@PathVariable String username1,
                                                              @PathVariable String username2) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();
            // todo if not logged then raise error


            PathDTO pathDTO = userService.getFollowPath(username1, username2);
            String msg = pathDTO.getDistance() < 0
                    ? "No vulnerable dependencies found"
                    : "Vulnerability path retrieved";
            return ResponseEntity.ok(new ResponseDTO<>(true, msg, pathDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding distance: " + e.getMessage(), null));
        }
    }

    // GET /api/distance/project/{username} : compute project distance between me and user {username}
    @GetMapping("/distance/project/{username}")
    public ResponseEntity<ResponseDTO<PathDTO>> getProjectPath(@PathVariable String username) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();
            // todo if not logged then raise error

            PathDTO pathDTO = userService.getProjectPath(authenticatedUsername, username);
            String msg = pathDTO.getDistance() < 0
                    ? "No vulnerable dependencies found"
                    : "Vulnerability path retrieved";
            return ResponseEntity.ok(new ResponseDTO<>(true, msg, pathDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding path: " + e.getMessage(), null));
        }
    }

    // GET /api/distance/project/{username}/{username} : compute project distance between two users
    @GetMapping("/distance/project/{username1}/{username2}")
    public ResponseEntity<ResponseDTO<PathDTO>> getProjectPathGeneric(@PathVariable String username1,
                                                              @PathVariable String username2) {
        try {
            // Get the authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            //System.out.println("Authenticated user: " + authenticatedUser);
            String authenticatedUsername = authenticatedUser.getUsername();
            // todo if not logged then raise error


            PathDTO pathDTO = userService.getProjectPath(username1, username2);
            String msg = pathDTO.getDistance() < 0
                    ? "No vulnerable dependencies found"
                    : "Vulnerability path retrieved";
            return ResponseEntity.ok(new ResponseDTO<>(true, msg, pathDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding path: " + e.getMessage(), null));
        }
    }

    // GET /api/project/vulnerability/{projectId} : discover vulnerabilities on project {id} from graph
    @GetMapping("/vulnerability/{projectId}")
    public ResponseEntity<ResponseDTO<PathDTO>> getVulnerabilityPath(
            @PathVariable String projectId) {

        try {
            Authentication auth = SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO<>(false, "Unauthorized", null));
            }

            PathDTO path = projectService.findVulnerabilityPath(projectId);
            String msg = path.getDistance() < 0
                    ? "No vulnerable dependencies found"
                    : "Vulnerability path retrieved";

            return ResponseEntity.ok(
                    new ResponseDTO<>(true, msg, path)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding path: " + e.getMessage(), null));
        }
    }

    // GET /api/project/vulnerability/{owner}/{projectName} : discover vulnerabilities on project {id} from graph
    @GetMapping("/vulnerability/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<PathDTO>> getVulnerabilityPathByOwnerAndName(
            @PathVariable String owner,
            @PathVariable String projectName) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO<>(false, "Unauthorized", null));
            }

            PathDTO path = projectService.findVulnerabilityPathByOwnerAndName(owner, projectName);
            String msg = path.getDistance() < 0
                    ? "No vulnerable dependencies found"
                    : "Vulnerability path retrieved";
            return ResponseEntity.ok(new ResponseDTO<>(true, msg, path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding dependencies: " + e.getMessage(), null));
        }
    }

    // GET /api/user/discover/projects : discover new projects (based on projects of pepole I follow)
    @GetMapping("/discover/projects")
    public ResponseEntity<ResponseDTO<List<it.unipi.githeritage.Model.Neo4j.Project>>> discoverProjects() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO<>(false, "Unauthorized", null));
            }
            String username = auth.getPrincipal().toString();
            List<it.unipi.githeritage.Model.Neo4j.Project> projects = projectService.discoverProjects(username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", projects));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding suggestions: " + e.getMessage(), null));
        }
    }

    // GET /api/user/discover/people : discover new people I can follow (friends of friends)
    // query parameters: username
    @GetMapping("/discover/people")
    public ResponseEntity<ResponseDTO<List<it.unipi.githeritage.Model.Neo4j.User>>> discoverPeople() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO<>(false, "Unauthorized", null));
            }
            String username = auth.getPrincipal().toString();
            List<it.unipi.githeritage.Model.Neo4j.User> users = userService.discoverPeople(username);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error finding suggestions: " + e.getMessage(), null));
        }
    }


    // SOLTANTO UN UTENTE CHE COLLABORA A QUESTO PROGETTO DEVE ESSERE IN GRADO DI VEDERE QUESTA COSA
    // GET /api/user/inefficiencies/{projectId} : discover inefficiencies in project {id} from graph
    // 20 metodi ordinati per hotness decrescente in un progetto
    @GetMapping("/inefficiencies/{projectId}")
    public ResponseEntity<ResponseDTO<List<Method>>> getInefficienciesByProjectId(
            @PathVariable String projectId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO<>(false, "Unauthorized", null));
            }
            String username = auth.getName();
            List<Method> methods = projectService.getInefficienciesByProjectId(username, projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Inefficiencies retrieved", methods));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error retrieving inefficiencies: " + e.getMessage(), null));
        }
    }

    // SOLTANTO UN UTENTE CHE COLLABORA A QUESTO PROGETTO DEVE ESSERE IN GRADO DI VEDERE QUESTA COSA
    // GET /api/user/inefficiencies/{owner}/{projectName} : discover inefficiencies in project {id} from graph
    // 20 metodi ordinati per hotness decrescente in un progetto
    @GetMapping("/inefficiencies/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<List<Method>>> getInefficienciesByOwnerAndName(
            @PathVariable String owner,
            @PathVariable String projectName) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO<>(false, "Unauthorized", null));
            }
            String username = auth.getName();
            List<Method> methods = projectService.getInefficienciesByOwnerAndName(username, owner, projectName);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Inefficiencies retrieved", methods));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error retrieving inefficiencies: " + e.getMessage(), null));
        }
    }
}
