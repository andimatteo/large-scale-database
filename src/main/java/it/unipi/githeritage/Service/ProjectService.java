package it.unipi.githeritage.Service;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import it.unipi.githeritage.DAO.MongoDB.ProjectMongoDAO;
import it.unipi.githeritage.DAO.MongoDB.UserMongoDAO;
import it.unipi.githeritage.DAO.Neo4j.Neo4jDAO;
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

    @Autowired
    private Neo4jDAO neo4jDAO;


    // create a new project
    public ProjectDTO addProject(ProjectDTO projectDTO) {
        ClientSession session = mongoClient.startSession();
        ProjectDTO newProject = null;
        try {
            session.startTransaction();
            
            // Save the project in MongoDB first
            newProject = projectMongoDAO.addProject(projectDTO);
            
            // Save the project in Neo4j
            neo4jDAO.addProject(newProject);

            session.commitTransaction();
            return newProject;
            
        } catch (Exception e) {
            session.abortTransaction();
            // If Neo4j failed but MongoDB succeeded, we should handle cleanup
            // For now, just return null
            return null;
        } finally {
            session.close();
        }
    }

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
        return opt.isPresent() ? opt.get().toDTO(opt.get()) : null; // TODO controllare che abbia senso
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

    public List<String> getFirstLevelDeps(String projectId) {
        return neo4jDAO.firstLevelDependencies(projectId);
    }

    public List<String> getAllRecursiveDeps(String projectId) {
        return neo4jDAO.recursiveDependencies(projectId);
    }

    public List<String> getAllRecursiveDepsPaginated(String projectId, int page) {
        return neo4jDAO.recursiveDependenciesPaginated(projectId,page);
    }

    public List<String> getAllMethods(String projectId) {
        return neo4jDAO.projectMethods(projectId);
    }

    public List<String> getAllMethodsPaginated(String projectId, int page) {
        return neo4jDAO.projectMethodsPaginated(projectId, page);
    }

    public ProjectDTO updateProject(ProjectDTO projectDTO, String authenticatedUsername) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();

            String projectId = projectDTO.getId();
            
            // First, get the existing project to check ownership
            ProjectDTO existingProject = getProjectById(projectId);
            if (existingProject == null) {
                throw new RuntimeException("Project not found with id: " + projectId);
            }
            
            if (!existingProject.getAdministrators().contains(authenticatedUsername)) {
                throw new RuntimeException("User is not authorized to update this project.");
            }
            
            // Set the project ID to ensure we're updating the correct project
            projectDTO.setId(projectId);
            
            // Update the project in MongoDB
            ProjectDTO updatedProject = projectMongoDAO.updateProject(projectDTO);
            
            // Update the project in Neo4j if needed
            neo4jDAO.updateProject(updatedProject);
            
            session.commitTransaction();
            return updatedProject;
            
        } catch (Exception e) {
            session.abortTransaction();
            throw new RuntimeException("Failed to update project: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

}
