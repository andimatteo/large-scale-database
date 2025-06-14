package it.unipi.githeritage.Service;

import com.mongodb.client.MongoClient;
import it.unipi.githeritage.DAO.MongoDB.ProjectMongoDAO;
import it.unipi.githeritage.DAO.MongoDB.UserMongoDAO;
import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Commit;
import it.unipi.githeritage.Model.MongoDB.Project;
import it.unipi.githeritage.Repository.MongoDB.MongoProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ProjectService {

    @Autowired
    private UserMongoDAO userMongoDAO;

    @Autowired
    private final MongoClient mongoClient;

    @Autowired
    private final MongoProjectRepository mongoProjectRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProjectMongoDAO projectMongoDAO;


    private CommitDTO mapCommit(Commit c) {
        CommitDTO dto = new CommitDTO();
        dto.setId(c.getId());
        dto.setCommitHash(c.getCommitHash());
        dto.setMessage(c.getMessage());
        dto.setTimestamp(c.getTimestamp());
        return dto;
    }

    public ProjectDTO getProjectById(String id) {
        Optional<Project> opt = mongoProjectRepository.findById(id);
        return opt.map(Project::toDTO).orElse(null);
    }

    public List<CommitDTO> getLast40Commits(String id) {
        return mongoProjectRepository.findById(id)
                .map(project -> project.getCommits().stream()
                        .sorted(Comparator.comparing(Commit::getTimestamp).reversed())
                        .limit(40)
                        .map(this::mapCommit)
                        .toList())
                .orElse(List.of());
    }

    public List<CommitDTO> getCommitsByPage(String id, int page) {
        int skip = page * 20;

        return mongoProjectRepository.findById(id)
                .map(project -> project.getCommits().stream()
                        .sorted(Comparator.comparing(Commit::getTimestamp).reversed())
                        .skip(skip)
                        .limit(20)
                        .map(this::mapCommit)
                        .toList())
                .orElse(List.of());
    }

    public UserActivityDistributionDTO getUserActivityDistribution(String username) {
        return projectMongoDAO.getUserActivityDistribution(username);
    }

    public List<LeaderboardProjectDTO> getAllTimeLeaderboard() {
        return projectMongoDAO.getAllTimeLeaderboard();
    }

    public List<LeaderboardProjectDTO> getLeaderboardLastMonths(int months) {
        return projectMongoDAO.getLeaderboardLastMonths(months);
    }

    public List<ContribDTO> getAllTimeContriboard() {
        return projectMongoDAO.getAllTimeContriboard();
    }

    public List<ContribDTO> getLastMonthsContriboard(int months) {
        return projectMongoDAO.getLastMonthsContriboard(months);
    }

    public List<ContribDTO> allTimeContributors(String projectId) {
        return projectMongoDAO.getAllTimeByProject(projectId);
    }

    public List<ContribDTO> lastMonthsContributors(String projectId, int months) {
        return projectMongoDAO.getLastMonthsByProject(projectId, months);
    }
}
