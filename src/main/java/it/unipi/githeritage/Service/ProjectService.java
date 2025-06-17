package it.unipi.githeritage.Service;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import it.unipi.githeritage.DAO.MongoDB.FileMongoDAO;
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
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;

import java.time.Instant;
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

    @Autowired
    private FileMongoDAO fileMongoDAO;

    // create a new project
    public ProjectDTO addProject(ProjectDTO projectDTO) {
        ClientSession session = mongoClient.startSession();
        ProjectDTO newProject = null;
        try {
            session.startTransaction();
            
            // Save the project in MongoDB first
            
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

    public ProjectDTO getProjectById(String id) {
        Optional<Project> opt = mongoProjectRepository.findById(id);
        return opt.isPresent() ? opt.get().toDTO(opt.get()) : null; // TODO controllare che abbia senso
    }

    public Optional<ProjectDTO> getProjectByOwnerAndName(String username, String projectName) {
        return mongoProjectRepository.findByOwnerAndName(username,projectName);
    }

    public List<Commit> getLast40Commits(String id) {
        return mongoProjectRepository.findById(id)
                .map(project -> project.getCommits().stream()
                        .sorted(Comparator.comparing(Commit::getTimestamp).reversed())
                        .limit(40)
                        .toList())
                .orElse(List.of());
    }

    public List<Commit> getLast40Commits(String owner, String projectName) {
        return mongoProjectRepository.findByOwnerAndName(owner,projectName)
                .map(project -> project.getCommits().stream()
                        .sorted(Comparator.comparing(Commit::getTimestamp).reversed())
                        .limit(40)
                        .toList())
                .orElse(List.of());
    }

    public List<Commit> getCommitsByPage(String id, int page) {
        int skip = page * 20;

        return mongoProjectRepository.findById(id)
                .map(project -> project.getCommits().stream()
                        .sorted(Comparator.comparing(Commit::getTimestamp).reversed())
                        .skip(skip)
                        .limit(20)
                        .toList())
                .orElse(List.of());
    }

    public List<Commit> getCommitsByPage(String owner, String projectName, int page) {
        int skip = page * 20;

        return mongoProjectRepository.findByOwnerAndName(owner,projectName)
                .map(project -> project.getCommits().stream()
                        .sorted(Comparator.comparing(Commit::getTimestamp).reversed())
                        .skip(skip)
                        .limit(20)
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

//    public ProjectDTO deleteProject(String projectId, String authenticatedUser) {
//        // delete project from mongoDB with all its files
//
//
//        // delete project from neo4j with all its methods
//    }
//
//    public ProjectDTO deleteProject(String owner, String projectId, String authenticatedUser) {
//        // delete project from mongoDB with all its files
//
//        // delete project from neo4j with all its methods
//    }

//    public ProjectDTO updateProject(CommitDTO commitDTO, String authenticatedUsername) {
//        ClientSession session = mongoClient.startSession();
//        try {
//            session.startTransaction();
//
//            String projectId = commitDTO.getProjectId();
//            // 1) prendi il progetto e controlla che esista e che l’utente sia admin
//            ProjectDTO existing = projectMongoDAO.findById(projectId);
//            if (existing == null) {
//                throw new RuntimeException("Project not found: " + projectId);
//            }
//            if (!existing.getAdministrators().contains(authenticatedUsername)) {
//                throw new RuntimeException("Forbidden: not an administrator");
//            }
//
//            // 2) applica le modifiche ai file
//            List<FileChangeDTO> changes = commitDTO.getFileChanges();
//            int linesAdded = 0, linesDeleted = 0, filesModified = 0;
//
//            for (FileChangeDTO change : changes) {
//                FileDTO file = change.getFile();
//                String path = file.getPath();
//                switch (change.getAction()) {
//
//                    case "POST":
//                        // crea nuovo file: non deve già esistere
//                        if (fileMongoDAO.fileExists(projectId, path)) {
//                            throw new RuntimeException("File already exists: " + path);
//                        }
//                        fileMongoDAO.createFile(projectId, file);
//                        linesAdded += file.getContent().split("\r?\n").length;
//                        filesModified++;
//
//                        // crea tutti i nodi su neo4j
//
//                        break;
//
//                    case "PUT":
//                        // verifica che il file esista
//                        if (!fileMongoDAO.fileExists(projectId, path)) {
//                            throw new RuntimeException("File not found for update: " + path);
//                        }
//
//                        // 1) leggi l'entity esistente (che contiene il suo content)
//                        FileDTO oldFile = fileMongoDAO.findFile(projectId, path);
//                        List<String> originalLines = Arrays.asList(
//                                oldFile.getContent().split("\\r?\\n", -1)
//                        );
//
//                        // 2) prepara le nuove righe
//                        List<String> revisedLines = Arrays.asList(
//                                file.getContent().split("\\r?\\n", -1)
//                        );
//
//                        // 3) calcola il patch
//                        Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);
//
//                        // 4) conta aggiunte e cancellazioni
//                        for (AbstractDelta<String> delta : patch.getDeltas()) {
//                            DeltaType type = delta.getType();
//                            switch (type) {
//                                case INSERT:
//                                    linesAdded   += delta.getTarget().size();
//                                    break;
//                                case DELETE:
//                                    linesDeleted += delta.getSource().size();
//                                    break;
//                                case CHANGE:
//                                    linesDeleted += delta.getSource().size();
//                                    linesAdded   += delta.getTarget().size();
//                                    break;
//                            }
//                        }
//
//                        // 5) aggiorna il file su Mongo
//                        fileMongoDAO.updateFile(file);
//
//                        // 6) ottieni tutti i metodi all'interno del file vecchio
//
//
//                        // 7) ottieni tutti i metodi all'interno del file nuovo
//
//
//                        // 8) aggiorna i metodi (rimuovi metodi non piu' presenti
//                        // inserisci metodi nuovi, modifica le connessioni con gli altri metodi)
//
//                        filesModified++;
//                        break;
//
//                    case "DELETE":
//                        // elimina file esistente
//                        if (!fileMongoDAO.fileExists(projectId, path)) {
//                            throw new RuntimeException("File not found for delete: " + path);
//                        }
//
//                        // cercare file per
//                        // username
//                        // nome del progetto
//                        // path del file
//
//                        // ottenere tutti quanti i metodi all'interno del file
//
//                        // procedere ad eliminare tutti i metodi anche da neo4j
//
//                        int removedLines = fileMongoDAO.countFileLines(projectId, path);
//                        fileMongoDAO.deleteFile(projectId, path);
//                        linesDeleted += removedLines;
//                        filesModified++;
//                        break;
//
//                    default:
//                        throw new RuntimeException("Unsupported action: " + change.getAction());
//                }
//            }
//
//            // 4) registra un nuovo Commit
//            Commit commit = new Commit();
//            commit.setUsername(authenticatedUsername);
//            commit.setFilesModified(filesModified);
//            commit.setLinesAdded(linesAdded);
//            commit.setLinesDeleted(linesDeleted);
//            commit.setCommitHash(UUID.randomUUID().toString());
//            commit.setTimestamp(Instant.now());
//            commitMongoDAO.insertCommit(session, commit);
//
//            session.commitTransaction();
//            return updated;
//
//        } catch (Exception e) {
//            session.abortTransaction();
//            throw new RuntimeException("Commit failed: " + e.getMessage(), e);
//        } finally {
//            session.close();
//        }
//    }
}
