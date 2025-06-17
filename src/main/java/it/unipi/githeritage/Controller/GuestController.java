package it.unipi.githeritage.Controller;

import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Commit;
import it.unipi.githeritage.Model.MongoDB.File;
import it.unipi.githeritage.Service.FileService;
import it.unipi.githeritage.Service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unipi.githeritage.Service.UserService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/guest")
@AllArgsConstructor
public class GuestController {

    @Autowired
    private final UserService userService;


    @Autowired
    private final ProjectService projectService;


    @Autowired
    private final FileService fileService;

    //////////////////////////////////////
    //         CRUD OPERATIONS          //
    //////////////////////////////////////

    //////////////////////////////////////
    //              USER                //
    //////////////////////////////////////

    // POST /api/guest/user : create new user
    @PostMapping("/user")
    public ResponseEntity<ResponseDTO<UserDTO>> addUser(@RequestBody UserDTO userDTO) {
        // Here you would typically call a service to save the user to the database.
        // For now, we will just return the userDTO as a response.

        UserDTO addedUser = userService.addUser(userDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "User created successfully", addedUser));
    }

    // GET /api/guest/user/{username} : get info about user
    @GetMapping("/user/{username}")
    public ResponseEntity<ResponseDTO<UserDTO>> getUser(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",userService.getUser(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/user/{username}/distribution : get user info with commit distributions
    @GetMapping("/user/{username}/distribution")
    public ResponseEntity<ResponseDTO<UserActivityDistributionDTO>> getUserWithDistribution(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getUserActivityDistribution(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }

    //////////////////////////////////////
    //             PROJECT              //
    //////////////////////////////////////

    // GET /api/guest/project/{id} : get project info
    @GetMapping("/project/{id}")
    public ResponseEntity<ResponseDTO<ProjectDTO>> getProject(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getProjectById(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching project: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/project/{username}/{projectName} : get project info
    @GetMapping("/project/{username}/{projectName}")
    public ResponseEntity<ResponseDTO<Optional<ProjectDTO>>> getProject(@PathVariable String username,
                                                                        @PathVariable String projectName) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getProjectByOwnerAndName(username,projectName)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching project: "
                            + e.getMessage(),null));
        }
    }

    // per quanto riguarda i commit non e' necessario un DTO, viene ritornato esattamente
    // il documento presente in mongoDB

    //////////////////////////////////////
    //             COMMIT               //
    //////////////////////////////////////

    // GET /api/guest/commit
    @GetMapping("/commit/{projectId}")
    public ResponseEntity<ResponseDTO<List<Commit>>> getLast40Commits(@PathVariable String projectId) {
        try {
            List<Commit> commits = projectService.getLast40Commits(projectId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", commits));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving commits for project " + projectId + ": " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/commit/{owner}/{projectName} : get last commits (40) for project
    @GetMapping("/commit/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<List<Commit>>> getLast40Commits(@PathVariable String owner,
                                                                      @PathVariable String projectName) {
        try {
            List<Commit> commits = projectService.getLast40Commits(owner,projectName);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", commits));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving commits for project /" + owner + '/' + projectName + ": " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/project/commit/{page}?projectId={projectId} : see all commits for project (paginated in
    //                                                                      pages of 20)
    @GetMapping("/commit/{projectId}/{page}")
    public ResponseEntity<ResponseDTO<List<Commit>>> getCommitsByPage(
            @PathVariable String projectId,
            @PathVariable int page) {
        try {
            List<Commit> commits = projectService.getCommitsByPage(projectId, page);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", commits));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving page " + page + " of commits for project " + projectId + ": "
                                    + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/project/commit/{page}?projectId={projectId} : see all commits for project (paginated in
    //                                                                      pages of 20)
    @GetMapping("/commit/{owner}/{projectName}/{page}")
    public ResponseEntity<ResponseDTO<List<Commit>>> getCommitsByPage(@PathVariable String owner,
                                                                      @PathVariable String projectName,
                                                                      @PathVariable int page) {
        try {
            List<Commit> commits = projectService.getCommitsByPage(owner, projectName, page);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", commits));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving page " + page + " of commits for project /" + owner +
                                    '/' + projectName + ": " + e.getMessage(),
                            null));
        }
    }

    //////////////////////////////////////
    //             FILE                 //
    //////////////////////////////////////

    // GET /api/guest/file/{id} : get file info
    @GetMapping("/file/{id}")
    public ResponseEntity<ResponseDTO<File>> getFileMetadata(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",fileService.getFile(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/file/{id} : get file info
    @GetMapping("/file/{username}/{projectName}/{path:**}")
    public ResponseEntity<ResponseDTO<File>> getFileMetadata(
            @PathVariable String username,
            @PathVariable String projectName,
            @PathVariable String path
    ) {
        System.out.println(path);
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",fileService.getFile(username,projectName,path)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/followers?username=username : get all user followers
    @GetMapping("/followers")
    public ResponseEntity<ResponseDTO<List<String>>> getFollowers(@RequestParam String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",userService.getFollowersUsernames(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving followers: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/follows?username=username : get all followed by user
    @GetMapping("/follows")
    public ResponseEntity<ResponseDTO<List<String>>> getFollows(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",userService.getFollowsUsernames(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving follows: "
                            + e.getMessage(),null));
        }
    }

    // questi rimangono commentati per il momento

    // GET /api/guest/leaderboard/projects : get all time leaderboard (progetti con la media piu' alta)
    @GetMapping("/leaderboard")
    public ResponseEntity<ResponseDTO<List<LeaderboardProjectDTO>>> allTimeLeaderboard() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getAllTimeLeaderboard()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving leaderboard: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/leaderboard/{monthts} : get all time leaderboard (progetti con la media
    // piu' alta negli ultimi months months)
    @GetMapping("/leaderboard/{months}")
    public ResponseEntity<ResponseDTO<List<LeaderboardProjectDTO>>> lastMonthsLeaderboard(@PathVariable int months) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getLeaderboardLastMonths(months)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving partial leaderboard: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/contriboard : get all time top contributors within platform
    @GetMapping("/contriboard")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getAllTimeContriboard() {
        try {
            List<ContribDTO> board = projectService.getAllTimeContriboard();
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/contriboard/{months} : get last months top contributors within all platform
    @GetMapping("/contriboard/{months}")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getLastMonthsContriboard(
            @PathVariable int months) {
        try {
            List<ContribDTO> board = projectService.getLastMonthsContriboard(months);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing last " + months + " months leaderboard: "
                                    + e.getMessage(),
                            null));
        }
    }


    // GET /api/guest/contributors?projectId=projectId : get leaderboard of contributors for project (all-time)
    @GetMapping("/contributors")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getAllTimeContributors(
            @RequestParam String projectId
    ) {
        try {
            List<ContribDTO> board = projectService.allTimeContributors(projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }


    // GET /api/guest/contributors/{months}?projectId=projectId : get leaderboard of contributors for project
    // last months months
    @GetMapping("/contributors/{months}")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getRecentContributors(
            @PathVariable int months,
            @RequestParam String projectId
    ) {
        try {
            List<ContribDTO> board = projectService.lastMonthsContributors(projectId, months);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/dependencies?projectId=projectId : list all 1st level dependencies for project
    @GetMapping("/dependencies")
    public ResponseEntity<ResponseDTO<List<String>>> getFirstLevelDependencies(
            @RequestParam String projectId
    ) {
        try {
            List<String> deps = projectService.getFirstLevelDeps(projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/dependencies/recursive?projectId=projectId : list all project dependencies (first 200)
    @GetMapping("/dependencies/recursive")
    public ResponseEntity<ResponseDTO<List<String>>> getRecursiveDependencies(
            @RequestParam String projectId
    ) {
        try {
            List<String> deps = projectService.getAllRecursiveDeps(projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }
    // GET /api/guest/dependencies/recursive/{page}?projectId=projectId : list all project dependencies in pages of 100
    @GetMapping("/dependencies/recursive/{page}")
    public ResponseEntity<ResponseDTO<List<String>>> getRecursiveDependenciesPaginated(
            @RequestParam String projectId,
            @PathVariable int page
    ) {
        try {
            List<String> deps = projectService.getAllRecursiveDepsPaginated(projectId,page);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/methods : list all project methods
    // query parameters: projectId
    @GetMapping("/methods")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectMethods(
            @RequestParam String projectId
    ) {
        try {
            List<String> methods = projectService.getAllMethods(projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", methods));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/methods/{page} : list all project methods in pages of 100
    // query parameters: projectId
    @GetMapping("/methods/{page}")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectMethodsPaginated(
            @RequestParam String projectId,
            @PathVariable int page
    ) {
        try {
            List<String> methods = projectService.getAllMethodsPaginated(projectId,page);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", methods));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }


}
