package it.unipi.githeritage.Controller;

import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Service.FileService;
import it.unipi.githeritage.Service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unipi.githeritage.Service.UserService;
import lombok.AllArgsConstructor;

import java.util.List;

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


    // GET /api/guest/commit?projectId={projectId} : get last commits (40) for project
    @GetMapping("/commit")
    public ResponseEntity<ResponseDTO<List<CommitDTO>>> getLast40Commits(
            @RequestParam("projectId") String projectId) {
        try {
            List<CommitDTO> commits = projectService.getLast40Commits(projectId);
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

    // GET /api/guest/project/commit/{page}?projectId={projectId} : see all commits for project (paginated in
    //                                                                      pages of 20)
    @GetMapping("/commit/{page}")
    public ResponseEntity<ResponseDTO<List<CommitDTO>>> getCommitsByPage(
            @RequestParam("projectId") String projectId,
            @PathVariable int page) {
        try {
            List<CommitDTO> commits = projectService.getCommitsByPage(projectId, page);
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

    // GET /api/guest/file/{id} : get file info
    @GetMapping("/file/{id}")
    public ResponseEntity<ResponseDTO<FileDTO>> getFileMetadata(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",fileService.getFileMetadata(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/gues/file/{id}/content : get file content
    @GetMapping("/file/{id}/content")
    public ResponseEntity<ResponseDTO<FileDTO>> getFileContent(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",fileService.getFileMetadata(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error searching user: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/files/search : search file within project by projectId and path
    @GetMapping("/file/search")
    public ResponseEntity<FileDTO> getFileByProjectAndPath(
            @RequestParam String projectId,
            @RequestParam String path) {
        return fileService
                .findByProjectIdAndPath(projectId, path)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // GET /api/guest/user/{id}/followers : get all user followers
    @GetMapping("/{username}/followers")
    public ResponseEntity<ResponseDTO<List<String>>> getFollowers(@PathVariable String username) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDTO<>(Boolean.TRUE,"",userService.getFollowersUsernames(username)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(Boolean.FALSE,"Error retrieving followers: "
                            + e.getMessage(),null));
        }
    }

    // GET /api/guest/user/{id}/follows : get all followed by user
    @GetMapping("/{username}/follows")
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
    // GET /api/guest/userboard : get all time leaderboard (utenti con la media piu' alta)
    // GET /api/guest/userboard/{months} : get leaderboard of last months months

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


    // GET /api/guest/contributors
    // request param projectId
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


    // get /api/gues/contributors/{months}
    // request param projectId
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

}
