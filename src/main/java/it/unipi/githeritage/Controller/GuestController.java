package it.unipi.githeritage.Controller;

import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Commit;
import it.unipi.githeritage.Model.MongoDB.File;
import it.unipi.githeritage.Model.MongoDB.Project;
import it.unipi.githeritage.Service.FileService;
import it.unipi.githeritage.Service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
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
    //              USER                //
    //////////////////////////////////////

    // POST /api/guest/user : create new user
    @PostMapping("/user")
    public ResponseEntity<ResponseDTO<UserDTO>> addUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO addedUser = userService.addUser(userDTO);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",addedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,"Error Creating User: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/user/{username} : get info about user
    @GetMapping("/user/{username}")
    public ResponseEntity<ResponseDTO<UserDTO>> getUser(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",userService.getUser(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }

    // read-concern: local
    // preference: nearest
    // todo test with commit data
    // GET /api/guest/user/{username}/distribution : get user info with commit distributions
    @GetMapping("/user/{username}/distribution")
    public ResponseEntity<ResponseDTO<List<DailyCommitCountDTO>>> getUserWithDistribution(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getUserActivityDistribution(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error computing user distribution: "
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching project: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/project/{username}/{projectName} : get project info
    @GetMapping("/project/{username}/{projectName}")
    public ResponseEntity<ResponseDTO<ProjectDTO>> getProject(@PathVariable String username,
                                                           @PathVariable String projectName) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getProjectByOwnerAndName(username,projectName)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching project: "
                            + e.getMessage(),null));
        }
    }


    // GET /api/guest/project/files/{username}/{projectName} : get project files (first 100)
    @GetMapping("/project/files/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectFiles(@PathVariable String owner,
                                                                     @PathVariable String projectName) {
        try {
            List<String> files = projectService.getAllFiles(owner,projectName);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", files));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving commits for project /" + owner + "/" + projectName + ": " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/project/files/{username}/{projectName}/{page} : get project files (paginated in pages of 50)
    @GetMapping("/project/files/{owner}/{projectName}/{page}")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectFiles(@PathVariable String owner,
                                                                     @PathVariable String projectName,
                                                                     @PathVariable Integer page) {
        try {
            List<String> files = projectService.getAllFilesPaginated(owner,projectName,page);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", files));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving commits for project /" + owner + "/" + projectName + ": " + e.getMessage(),
                            null));
        }
    }

    //////////////////////////////////////
    //             COMMIT               //
    //////////////////////////////////////


    // GET /api/guest/commit
    @GetMapping("/commit/{projectId}")
    public ResponseEntity<ResponseDTO<List<Commit>>> getLast100Commits(@PathVariable String projectId) {
        try {
            List<Commit> commits = projectService.getLast100Commits(projectId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", commits));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving commits for project " + projectId + ": " + e.getMessage(),
                            null));
        }
    }


    // GET /api/guest/commit/{owner}/{projectName} : get last commits (40) for project
    @GetMapping("/commit/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<List<Commit>>> getLast100Commits(@PathVariable String owner,
                                                                      @PathVariable String projectName) {
        try {
            List<Commit> commits = projectService.getLast100Commits(owner,projectName);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO<>(true, "", commits));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving commits for project /" + owner + '/' + projectName + ": " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/project/commit/{page}?projectId={projectId} : see all commits for project (paginated in
    //                                                                      pages of 100)
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
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error retrieving page " + page + " of commits for project " + projectId + ": "
                                    + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/project/commit/{page}?projectId={projectId} : see all commits for project (paginated in
    //                                                                      pages of 100)
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
                    .status(HttpStatus.FORBIDDEN)
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
    public ResponseEntity<ResponseDTO<File>> getFile(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",fileService.getFile(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }


    // GET /api/guest/file/{id} : get file info
    @GetMapping("/file/{username}/{projectName}/**")
    public ResponseEntity<ResponseDTO<File>> getFile(
            HttpServletRequest request,
            @PathVariable String username,
            @PathVariable String projectName
    ) {
        String prefix = username + "/" + projectName + "/";
        String fullPath = request.getRequestURI();
        String path = fullPath.substring(fullPath.indexOf(prefix) + prefix.length());
//        System.out.println(path);
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",fileService.getFile(username,projectName,path)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching file: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/followers?username=username : get all user followers
    @GetMapping("/followers/{username}")
    public ResponseEntity<ResponseDTO<List<String>>> getFollowers(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",userService.getFollowersUsernames(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving followers: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/follows?username=username : get all followed by user
    @GetMapping("/follows/{username}")
    public ResponseEntity<ResponseDTO<List<String>>> getFollows(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",userService.getFollowsUsernames(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving follows: "
                            + e.getMessage(),null));
        }
    }

    // read-concern: local
    // preference: nearest
    // GET /api/guest/leaderboard/projects : get all time leaderboard (progetti con la media piu' alta)
    @GetMapping("/leaderboard")
    public ResponseEntity<ResponseDTO<List<LeaderboardProjectDTO>>> allTimeLeaderboard() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getAllTimeLeaderboard()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving leaderboard: "
                            + e.getMessage(),null));
        }
    }

    // read-concern: local
    // preference: nearest
    // GET /api/guest/leaderboard/{monthts} : get all time leaderboard (progetti con la media
    // piu' alta negli ultimi months months)
    @GetMapping("/leaderboard/{months}")
    public ResponseEntity<ResponseDTO<List<LeaderboardProjectDTO>>> lastMonthsLeaderboard(@PathVariable int months) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",projectService.getLeaderboardLastMonths(months)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving partial leaderboard: "
                            + e.getMessage(),null));
        }
    }


    // read-concern: local
    // preference: nearest
    // GET /api/guest/contriboard : get all time top contributors within platform
    @GetMapping("/contriboard")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getAllTimeContriboard() {
        try {
            List<ContribDTO> board = projectService.getAllTimeContriboard();
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time contribution leaderboard: " + e.getMessage(),
                            null));
        }
    }


    // read-concern: local
    // preference: nearest
    // GET /api/guest/contriboard/{months} : get last months top contributors within all platform
    @GetMapping("/contriboard/{months}")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getLastMonthsContriboard(
            @PathVariable int months) {
        try {
            List<ContribDTO> board = projectService.getLastMonthsContriboard(months);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing last " + months + " months contribution leaderboard: "
                                    + e.getMessage(),
                            null));
        }
    }



    // read-concern: local
    // preference: nearest
    // GET /api/guest/contributors/{projectId} : get leaderboard of contributors for project
    @GetMapping("/contributors/{projectId}/{months}")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getAllTimeContributors(
            @PathVariable String projectId,
            @PathVariable Integer months
    ) {
        if (months == null) {
            months = 0;
        }
        try {
            List<ContribDTO> board = projectService.allTimeContributorsId(projectId,months);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard for project " + projectId
                                    + ": " + e.getMessage(),
                            null));
        }
    }



    // read-concern: local
    // preference: nearest
    // GET /api/guest/contributors/{owner}/{projectName}?months=months : get leaderboard of contributors for project
    @GetMapping("/contributors/{owner}/{projectName}/{months}")
    public ResponseEntity<ResponseDTO<List<ContribDTO>>> getAllTimeContributors(
            @PathVariable String owner,
            @PathVariable String projectName,
            @PathVariable Integer months
    ) {
        if (months == null) {
            months = 0;
        }
        try {
            List<ContribDTO> board = projectService.allTimeContributorsOwnerProjectName(owner, projectName, months);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", board));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard for project /" + owner +
                            '/' + projectName + ": " + e.getMessage(),
                            null));
        }
    }

    // todo test con dati
    // GET /api/guest/dependencies?projectId=projectId : list all 1st level dependencies for project
    @GetMapping("/dependencies/{projectId}")
    public ResponseEntity<ResponseDTO<List<String>>> getFirstLevelDependencies(
            @PathVariable String projectId
    ) {
        try {
            List<String> deps = projectService.getFirstLevelDeps(projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }
    // GET /api/guest/dependencies?projectId=projectId : list all 1st level dependencies for project
    @GetMapping("/dependencies/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<List<String>>> getFirstLevelDependencies(
            @PathVariable String owner,
            @PathVariable String projectName
    ) {
        try {
            List<String> deps = projectService.getFirstLevelDeps(owner,projectName);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // todo rivedere query con neo4j
    // GET /api/guest/dependencies/recursive?projectId=projectId : list all project dependencies (first 200)
    @GetMapping("/dependencies/recursive/{projectId}")
    public ResponseEntity<ResponseDTO<List<String>>> getRecursiveDependencies(
            @PathVariable String projectId
    ) {
        try {
            List<String> deps = projectService.getAllRecursiveDeps(projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    @GetMapping("/dependencies/recursive/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<List<String>>> getRecursiveDependencies(
            @PathVariable String owner,
            @PathVariable String projectName
    ) {
        try {
            List<String> deps = projectService.getAllRecursiveDeps(owner,projectName);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // todo rivedere query con neo4j
    // GET /api/guest/dependencies/recursive/{projectId}/{page} : list all project dependencies in pages of 100
    @GetMapping("/dependencies/recursive/{projectId}/{page}")
    public ResponseEntity<ResponseDTO<List<String>>> getRecursiveDependenciesPaginated(
            @PathVariable String projectId,
            @PathVariable int page
    ) {
        try {
            List<String> deps = projectService.getAllRecursiveDepsPaginated(projectId,page);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/dependencies/recursive/{projectId}/{page} : list all project dependencies in pages of 100
    @GetMapping("/dependencies/recursive/{owner}/{projectName}/{page}")
    public ResponseEntity<ResponseDTO<List<String>>> getRecursiveDependenciesPaginated(
            @PathVariable String owner,
            @PathVariable String projectName,
            @PathVariable int page
    ) {
        try {
            List<String> deps = projectService.getAllRecursiveDepsPaginated(owner,projectName,page);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", deps));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // todo rivedere query con neo4j
    // GET /api/guest/methods/{projectId} : list all project methods
    @GetMapping("/methods/{projectId}")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectMethods(
            @PathVariable String projectId
    ) {
        try {
            List<String> methods = projectService.getAllMethods(projectId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", methods));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/methods/{projectId} : list all project methods
    @GetMapping("/methods/{owner}/{projectName}")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectMethods(
            @PathVariable String owner,
            @PathVariable String projectName
    ) {
        try {
            List<String> methods = projectService.getAllMethods(owner,projectName);
            return ResponseEntity.ok(new ResponseDTO<>(true, "", methods));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // todo rivedere query con neo4j
    // GET /api/guest/methods/{projectId}/{page} : list all project methods in pages of 100
    @GetMapping("/methods/{projectId}/{page}")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectMethodsPaginated(
            @PathVariable String projectId,
            @PathVariable int page
    ) {
        try {
            List<String> methods = projectService.getAllMethodsPaginated(projectId,page);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Methods found", methods));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }

    // GET /api/guest/methods/{projectId}/{page} : list all project methods in pages of 100
    @GetMapping("/methods/{owner}/{projectName}/{page}")
    public ResponseEntity<ResponseDTO<List<String>>> getProjectMethodsPaginated(
            @PathVariable String owner,
            @PathVariable String projectName,
            @PathVariable int page
    ) {
        try {
            List<String> methods = projectService.getAllMethodsPaginated(owner,projectName,page);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Methods found", methods));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ResponseDTO<>(false,
                            "Error computing all-time leaderboard: " + e.getMessage(),
                            null));
        }
    }
}
