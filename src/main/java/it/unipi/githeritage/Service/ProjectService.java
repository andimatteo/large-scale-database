package it.unipi.githeritage.Service;

import com.google.common.collect.Sets;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.netty.channel.unix.RawUnixChannelOption;
import it.unipi.githeritage.DAO.MongoDB.CommitMongoDAO;
import it.unipi.githeritage.DAO.MongoDB.ProjectMongoDAO;
import it.unipi.githeritage.DAO.Neo4j.Neo4jDAO;
import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Commit;
import it.unipi.githeritage.Model.MongoDB.File;
import it.unipi.githeritage.Model.MongoDB.Project;
import it.unipi.githeritage.Model.Neo4j.Method;
import it.unipi.githeritage.Repository.MongoDB.MongoCommitRepository;
import it.unipi.githeritage.Repository.MongoDB.MongoFileRepository;
import it.unipi.githeritage.Repository.MongoDB.MongoProjectRepository;
import it.unipi.githeritage.Repository.MongoDB.MongoUserRepository;
import it.unipi.githeritage.Repository.Neo4j.NeoMethodRepository;
import it.unipi.githeritage.Repository.Neo4j.NeoProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectService {

    @Autowired
    private final MongoClient mongoClient;

    @Autowired
    private final MongoProjectRepository mongoProjectRepository;

    @Autowired
    private ProjectMongoDAO projectMongoDAO;

    @Autowired
    private Neo4jDAO neo4jDAO;

    @Autowired
    private MongoFileRepository mongoFileRepository;

    @Autowired
    private MongoCommitRepository mongoCommitRepository;

    @Autowired
    private NeoProjectRepository neoProjectRepository;

    @Autowired
    private NeoMethodRepository neoMethodRepository;

    @Autowired
    private CommitMongoDAO commitMongoDAO;
    @Autowired
    private MongoUserRepository mongoUserRepository;

    public ProjectDTO addProject(ProjectDTO projectDTO) {
        ClientSession session = mongoClient.startSession();
        Boolean neo = false;
        try {
            session.startTransaction();
            
            // Save the project in MongoDB first
            if (mongoProjectRepository.existsByOwnerAndName(projectDTO.getOwner(),projectDTO.getName())) {
                throw new RuntimeException("Project with same name already exists");
            }

            // give same id to neo4j node
            Project project = mongoProjectRepository.save(Project.fromDTO(projectDTO));
            projectDTO.setId(project.getId());

            // Save the project in Neo4j
            neo4jDAO.addProject(projectDTO);
            neo = true;

            session.commitTransaction();

            return projectDTO;
            
        } catch (Exception e) {
            session.abortTransaction();
            if (neo) {
                throw new RuntimeException("Project creation succeded in Neo4j but failed in MongoDB, check for " +
                        "consistency in database");
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public ProjectDTO getProjectById(String id) {
        Optional<Project> opt = mongoProjectRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Project with id " + id + " not found");
        }
        Project project = opt.get();
        ProjectDTO dto = project.toDTO(project);
        dto.setCommitsCount(project.getCommitIds() != null ? project.getCommitIds().size() : 0);
        dto.setFilesCount(project.getFileIds() != null ? project.getFileIds().size() : 0);
        dto.setComments(project.getComments()); // opzionale, se già presenti
        return dto;
    }


    public ProjectDTO getProjectByOwnerAndName(String username, String projectName) {
        Optional<Project> opt = mongoProjectRepository.findByOwnerAndName(username, projectName);
        if (opt.isEmpty()) {
            throw new RuntimeException("Project /" + username + '/' + projectName + " not found");
        }
        Project project = opt.get();
        ProjectDTO dto = project.toDTO(project);
        dto.setCommitsCount(project.getCommitIds() != null ? project.getCommitIds().size() : 0);
        dto.setFilesCount(project.getFileIds() != null ? project.getFileIds().size() : 0);
        dto.setComments(project.getComments()); // opzionale, se già presenti
        return dto;
    }

    public List<Commit> getLast100Commits(String projectId) {
        return commitMongoDAO.getCommitsPaginated(projectId,0,100);
    }

    public List<Commit> getLast100Commits(String owner, String projectName) {
        return commitMongoDAO.getCommitsPaginated(owner,projectName,0,100);
    }

    public List<Commit> getCommitsByPage(String projectId, int page) {
        return commitMongoDAO.getCommitsPaginated(projectId,page,100);
    }

    public List<Commit> getCommitsByPage(String owner, String projectName, int page) {
        return commitMongoDAO.getCommitsPaginated(owner,projectName,page,100);
    }

    public List<DailyCommitCountDTO> getUserActivityDistribution(String username) {
        return projectMongoDAO.getUserDailyActivity(username);
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

    public List<ContribDTO> allTimeContributorsId(String projectId, int months) {
        return projectMongoDAO.getAllTimeByProject(projectId,months);
    }

    public List<ContribDTO> allTimeContributorsOwnerProjectName(String owner, String projectName, int months) {
        return projectMongoDAO.getAllTimeByProject(owner,projectName,months);
    }

    public List<String> getFirstLevelDeps(String projectId) {
        return neo4jDAO.firstLevelDependencies(projectId);
    }

    public List<String> getFirstLevelDeps(String owner, String projectName) {
        return neo4jDAO.firstLevelDependencies(owner,projectName);
    }

    public List<String> getAllRecursiveDeps(String projectId) {
        return neo4jDAO.recursiveDependencies(projectId);
    }

    public List<String> getAllRecursiveDeps(String owner, String projectName) {
        return neo4jDAO.recursiveDependencies(owner,projectName);
    }

    public List<String> getAllRecursiveDepsPaginated(String projectId, int page) {
        return neo4jDAO.recursiveDependenciesPaginated(projectId,page);
    }

    public List<String> getAllRecursiveDepsPaginated(String owner, String projectName, int page) {
        return neo4jDAO.recursiveDependenciesPaginated(owner,projectName,page);
    }

    public List<String> getAllMethods(String projectId) {
        return neo4jDAO.projectMethods(projectId);
    }

    public List<String> getAllMethods(String owner, String projectName) {
        return neo4jDAO.projectMethods(owner, projectName);
    }

    public List<String> getAllMethodsPaginated(String projectId, int page) {
        return neo4jDAO.projectMethodsPaginated(projectId, page);
    }

    public List<String> getAllMethodsPaginated(String owner, String projectName, int page) {
        return neo4jDAO.projectMethodsPaginated(owner, projectName, page);
    }

    public ProjectDTO deleteProject(String projectId, String authenticatedUser, boolean isAdmin) {
        // 1) prendo document da Mongo
        Project project = mongoProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return deleteProject(
                project.getOwner(),
                project.getName(),
                projectId,
                authenticatedUser,
                isAdmin
        );
    }

    public ProjectDTO deleteProject(String owner,
                                    String projectName,
                                    String authenticatedUser,
                                    Boolean isAdmin) {
        // cerco in Mongo con owner+name
        Project project = mongoProjectRepository
                .findByOwnerAndName(owner, projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return deleteProject(
                owner,
                projectName,
                project.getId(),
                authenticatedUser,
                isAdmin
        );
    }

    private ProjectDTO deleteProject(String owner,
                                     String projectName,
                                     String projectId,
                                     String authenticatedUser,
                                     Boolean isAdmin
                                    ) {
        // 1) utente non negli amministratori e utente non admin
        if (!mongoProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"))
                .getAdministrators().contains(authenticatedUser)
            && !isAdmin) {
            throw new RuntimeException("Forbidden");
        }

        boolean neoDone   = false;
        try {

            // delete in mongoDB
            mongoFileRepository.deleteByOwnerAndProjectName(owner, projectName);
            mongoProjectRepository.deleteById(projectId);

            // delete project node and all its methods if orphans
            List<String> methodList = neo4jDAO.projectMethodsById(projectId);
            neo4jDAO.deleteProjectNodeById(projectId);

            // safe delete: eliminazione di un nodo metodo soltanto se
            for (String fqn : methodList) {
                neo4jDAO.clearMethodCalls(owner, fqn);
                neo4jDAO.deleteMethodIfOrphan(owner, fqn);
            }
            neoDone = true;

        } catch (Exception e) {

            if (neoDone) {
                throw new RuntimeException("Neo4j project deletion completed but mongoDB failed," +
                        " check for consistency");
            }

            // bring exception to higher level
            throw new RuntimeException(e.getMessage());

        }

        // 6) restituisco conferma
        ProjectDTO dto = new ProjectDTO();
        dto.setId(projectId);
        dto.setName(projectName);
        dto.setOwner(owner);
        return dto;
    }

    // todo finish update project
    public Project updateProject(String projectId, CommitIdDTO commitIdDTO, String username, Boolean isAdmin) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();

            // 1) prendi il progetto e controlla che esista e che l’utente sia admin
            Project project = mongoProjectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            String owner = project.getOwner();
            String projectName = project.getName();

            // se utente non e' in lista di amministratori e non e' admin
            if (!project.getAdministrators().contains(username) && !isAdmin) {
                throw new RuntimeException("Forbidden: not an administrator");
            }

            // 2) applica le modifiche ai file
            List<FileWrapperDTO> changes = commitIdDTO.getFiles();
            int linesAdded = 0, linesDeleted = 0, filesModified = 0;

            for (FileWrapperDTO change : changes) {
                File oldFile;
                String id;
                File file = change.getFile();

                // set modified timestamp
                file.setLastModified(Instant.now());

                // set who modified last modified the file
                file.setLastModifiedBy(username);


                String path = file.getPath();
                switch (change.getAction()) {

                    case "POST":
                        // 1) salvataggio Mongo
                        if (mongoFileRepository
                                .findByOwnerAndProjectNameAndPath(owner, projectName, path)
                                .isPresent()) {
                            throw new RuntimeException("File " + path + " already exists");
                        }
                        mongoFileRepository.save(file);
                        filesModified++;
                        linesAdded += file.getContent().split("\r?\n").length;

                        // 2) aggiorna Neo4j tramite DAO
                        // 2.1 nodi
                        neo4jDAO.mergeUser(owner);
                        neo4jDAO.mergeProject(projectId, projectName, owner);
                        neo4jDAO.relateUserToProject(owner, projectId);

                        // 2.2 metodi e HAS_METHOD
                        String content = file.getContent();
                        CompilationUnit cu = StaticJavaParser.parse(content);
                        for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
                            ResolvedMethodDeclaration rmd = md.resolve();
                            String fqn        = rmd.getQualifiedSignature();
                            String simpleName = rmd.getName();

                            neo4jDAO.mergeMethod(owner, fqn, simpleName);
                            neo4jDAO.relateProjectToMethod(projectId, owner, fqn);
                        }

                        // CALLS
                        for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
                            String callerFqn = md.resolve().getQualifiedSignature();
                            for (MethodCallExpr call : md.findAll(MethodCallExpr.class)) {
                                String calleeFqn = call.resolve().getQualifiedSignature();
                                neo4jDAO.relateMethodsCall(owner, callerFqn, calleeFqn);
                            }
                        }

                        // 2.4 dipendenze dinamiche tra progetti
                        cu.getPackageDeclaration().ifPresent(pd -> {
                            String pkg     = pd.getNameAsString();
                            String depProj = extractProjectFromPackage(pkg);
                            if (!depProj.equals(projectName)) {
                                neo4jDAO.relateProjectDependency(projectId, depProj);
                            }
                        });

                        break;

                    case "PUT":
                        // 1) verifica che il file esista
                        oldFile = mongoFileRepository.findByOwnerAndProjectNameAndPath(owner, projectName, path)
                                .orElseThrow(() -> new RuntimeException("File " + path + " not found"));
//                        id = oldFile.getId();


                        // 2) prendi il vecchio contenuto
                        List<String> originalLines = Arrays.asList(oldFile.getContent()
                                .split("\\r?\\n", -1));

                        // 3) prepara il nuovo contenuto
                        List<String> newLines = Arrays.asList(file.getContent()
                                .split("\\r?\\n", -1));

                        // 4) calcola patch e conta righe
                        Patch<String> patch = DiffUtils.diff(originalLines, newLines);
                        for (AbstractDelta<String> delta : patch.getDeltas()) {
                            switch (delta.getType()) {
                                case INSERT: linesAdded   += delta.getTarget().size(); break;
                                case DELETE: linesDeleted += delta.getSource().size(); break;
                                case CHANGE:
                                    linesDeleted += delta.getSource().size();
                                    linesAdded   += delta.getTarget().size();
                                    break;
                            }
                        }

                        // 5) aggiorna il file in Mongo
                        mongoFileRepository.save(file);

                        // estraggo FQN dei metodi dal vecchio file
                        CompilationUnit oldCu = StaticJavaParser.parse(oldFile.getContent());
                        Set<String> oldMethods = oldCu.findAll(MethodDeclaration.class).stream()
                                .map(md -> md.resolve().getQualifiedSignature())
                                .collect(Collectors.toSet());

                        // estraggo FQN e simpleName dei metodi dal nuovo file
                        String newContent = file.getContent();
                        CompilationUnit newCu = StaticJavaParser.parse(newContent);
                        Map<String,String> newMethods = newCu.findAll(MethodDeclaration.class).stream()
                                .collect(Collectors.toMap(
                                        // chiave: FQN
                                        md -> md.resolve().getQualifiedSignature(),
                                        // valore: nome semplice come String
                                        md -> md.getNameAsString(),
                                        // mergeFunction: in caso di duplicati, tieni il primo
                                        (String existing, String replacement) -> existing
                                ));

                        // 6) rimuovo i metodi ora spariti
                        for (String removedFqn : Sets.difference(oldMethods, newMethods.keySet())) {
                            neo4jDAO.removeProjectToMethod(projectId, owner, removedFqn);
                            // cancella metodo se orfano
                            neo4jDAO.deleteMethodIfOrphan(owner, removedFqn);
                        }

                        // 7) aggiungo i metodi nuovi
                        for (Map.Entry<String,String> entry : Sets.difference(newMethods.keySet(), oldMethods).stream()
                                .map(fqn -> Map.entry(fqn, newMethods.get(fqn)))
                                .collect(Collectors.toList())) {
                            String fqn        = entry.getKey();
                            String simpleName = entry.getValue();
                            
                            // Create new method declaration for analysis
                            Optional<MethodDeclaration> mdOpt = newCu.findAll(MethodDeclaration.class).stream()
                                .filter(md -> md.resolve().getQualifiedSignature().equals(fqn))
                                .findFirst();
                            
                            if (mdOpt.isPresent()) {
                                MethodMetrics metrics = analyzeMethodComplexity(mdOpt.get());
                                neo4jDAO.mergeMethodWithMetrics(owner, fqn, simpleName, 
                                                              metrics.assignmentCount, 
                                                              metrics.arithmeticCount, 
                                                              metrics.loopCount);
                            } else {
                                // Fallback to basic method creation
                                neo4jDAO.mergeMethod(owner, fqn, simpleName);
                            }
                            neo4jDAO.relateProjectToMethod(projectId, owner, fqn);
                        }

                        // 8) aggiorno tutte le CALLS per i metodi rimasti (intersezione)
                        Set<String> retained = Sets.intersection(oldMethods, newMethods.keySet());
                        for (String callerFqn : retained) {
                            // cancello vecchie relazioni CALLS
                            neo4jDAO.clearMethodCalls(owner, callerFqn);

                            // ristampo le chiamate dal nuovo AST
                            newCu.findAll(MethodDeclaration.class).stream()
                                    .filter(md -> md.resolve().getQualifiedSignature().equals(callerFqn))
                                    .flatMap(md -> md.findAll(MethodCallExpr.class).stream())
                                    .map(call -> call.resolve().getQualifiedSignature())
                                    .forEach(calleeFqn ->
                                            neo4jDAO.relateMethodsCall(owner, callerFqn, calleeFqn)
                                    );
                        }

                        filesModified++;
                        break;

                    case "DELETE":
                        // 1) verifica che il file esista
                        oldFile = mongoFileRepository.findByOwnerAndProjectNameAndPath(owner, projectName, path)
                                .orElseThrow(() -> new RuntimeException("File " + path + " not found"));
                        id = oldFile.getId();

                        // 2) estrazione metodi
                        CompilationUnit oldCuDel = StaticJavaParser.parse(oldFile.getContent());
                        Set<String> methodsToRemove = oldCuDel.findAll(MethodDeclaration.class).stream()
                                .map(md -> md.resolve().getQualifiedSignature())
                                .collect(Collectors.toSet());

                        // 3) eliminazione relazioni e metodi
                        for (String fqn : methodsToRemove) {
                            neo4jDAO.removeProjectToMethod(projectId, owner, fqn);
                            neo4jDAO.deleteMethodIfOrphan(owner, fqn);
                        }

                        // 4) elimino il file da Mongo e aggiorno i contatori
                        int removedLines = oldFile.getLines();
                        mongoFileRepository.deleteById(id);
                        linesDeleted += removedLines;
                        filesModified++;
                        break;

                    default:
                        throw new RuntimeException("Unsupported action: " + change.getAction());
                }
            }

            // 4) registra un nuovo Commit
            Commit commit = new Commit();
            commit.setFilesModified(filesModified);
            commit.setLinesAdded(linesAdded);
            commit.setLinesDeleted(linesDeleted);
            commit.setTimestamp(Instant.now());
            mongoCommitRepository.save(commit);

            session.commitTransaction();
            return project;

        } catch (Exception e) {
            session.abortTransaction();
            throw new RuntimeException("Commit failed: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    // todo finish update project
    public ProjectDTO updateProject(CommitOwnerDTO commitOwnerDTOt, String username) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();

            String owner = commitOwnerDTOt.getOwner();
            String projectName = commitOwnerDTOt.getProjectName();

            // 1) prendi il progetto e controlla che esista e che l’utente sia admin
            Project project = mongoProjectRepository.findByOwnerAndName(owner, projectName)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            String projectId = project.getId();

            if (!project.getAdministrators().contains(username)) {
                throw new RuntimeException("Forbidden: not an administrator");
            }

            // 2) applica le modifiche ai file
            List<FileWrapperDTO> changes = commitOwnerDTOt.getFiles();
            int linesAdded = 0, linesDeleted = 0, filesModified = 0;

            for (FileWrapperDTO change : changes) {
                File oldFile;
                String id;
                File file = change.getFile();

                // set modified timestamp
                file.setLastModified(Instant.now());

                // set who modified last modified the file
                file.setLastModifiedBy(username);


                String path = file.getPath();
                switch (change.getAction()) {

                    case "POST":
                        // 1) salvataggio Mongo
                        if (mongoFileRepository
                                .findByOwnerAndProjectNameAndPath(owner, projectName, path)
                                .isPresent()) {
                            throw new RuntimeException("File " + path + " already exists");
                        }
                        mongoFileRepository.save(file);
                        filesModified++;
                        linesAdded += file.getContent().split("\r?\n").length;

                        // 2) aggiorna Neo4j tramite DAO
                        // 2.1 nodi
                        neo4jDAO.mergeUser(owner);
                        neo4jDAO.mergeProject(projectId, projectName, owner);
                        neo4jDAO.relateUserToProject(owner, projectId);

                        // 2.2 metodi e HAS_METHOD
                        String content = file.getContent();
                        CompilationUnit cu = StaticJavaParser.parse(content);
                        for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
                            ResolvedMethodDeclaration rmd = md.resolve();
                            String fqn        = rmd.getQualifiedSignature();
                            String simpleName = rmd.getName();

                            neo4jDAO.mergeMethod(owner, fqn, simpleName);
                            neo4jDAO.relateProjectToMethod(projectId, owner, fqn);
                        }

                        // CALLS
                        for (MethodDeclaration md : cu.findAll(MethodDeclaration.class)) {
                            String callerFqn = md.resolve().getQualifiedSignature();
                            for (MethodCallExpr call : md.findAll(MethodCallExpr.class)) {
                                String calleeFqn = call.resolve().getQualifiedSignature();
                                neo4jDAO.relateMethodsCall(owner, callerFqn, calleeFqn);
                            }
                        }

                        // 2.4 dipendenze dinamiche tra progetti
                        cu.getPackageDeclaration().ifPresent(pd -> {
                            String pkg     = pd.getNameAsString();
                            String depProj = extractProjectFromPackage(pkg);
                            if (!depProj.equals(projectName)) {
                                neo4jDAO.relateProjectDependency(projectId, depProj);
                            }
                        });

                        break;

                    case "PUT":
                        // 1) verifica che il file esista
                        oldFile = mongoFileRepository.findByOwnerAndProjectNameAndPath(owner, projectName, path)
                                .orElseThrow(() -> new RuntimeException("File " + path + " not found"));
                        id = oldFile.getId();
                        file.setId(id);

                        // 2) prendi il vecchio contenuto
                        List<String> originalLines = Arrays.asList(oldFile.getContent()
                                .split("\\r?\\n", -1));

                        // 3) prepara il nuovo contenuto
                        List<String> newLines = Arrays.asList(file.getContent()
                                .split("\\r?\\n", -1));

                        // 4) calcola patch e conta righe
                        Patch<String> patch = DiffUtils.diff(originalLines, newLines);
                        for (AbstractDelta<String> delta : patch.getDeltas()) {
                            switch (delta.getType()) {
                                case INSERT: linesAdded   += delta.getTarget().size(); break;
                                case DELETE: linesDeleted += delta.getSource().size(); break;
                                case CHANGE:
                                    linesDeleted += delta.getSource().size();
                                    linesAdded   += delta.getTarget().size();
                                    break;
                            }
                        }

                        // 5) aggiorna il file in Mongo
                        mongoFileRepository.save(file);

                        // estraggo FQN dei metodi dal vecchio file
                        CompilationUnit oldCu = StaticJavaParser.parse(oldFile.getContent());
                        Set<String> oldMethods = oldCu.findAll(MethodDeclaration.class).stream()
                                .map(md -> md.resolve().getQualifiedSignature())
                                .collect(Collectors.toSet());

                        // estraggo FQN e simpleName dei metodi dal nuovo file
                        String newContent = file.getContent();
                        CompilationUnit newCu = StaticJavaParser.parse(newContent);
                        Map<String,String> newMethods = newCu.findAll(MethodDeclaration.class).stream()
                                .collect(Collectors.toMap(
                                        // chiave: FQN
                                        md -> md.resolve().getQualifiedSignature(),
                                        // valore: nome semplice come String
                                        md -> md.getNameAsString(),
                                        // mergeFunction: in caso di duplicati, tieni il primo
                                        (String existing, String replacement) -> existing
                                ));

                        // 6) rimuovo i metodi ora spariti
                        for (String removedFqn : Sets.difference(oldMethods, newMethods.keySet())) {
                            neo4jDAO.removeProjectToMethod(projectId, owner, removedFqn);
                            // cancella metodo se orfano
                            neo4jDAO.deleteMethodIfOrphan(owner, removedFqn);
                        }

                        // 7) aggiungo i metodi nuovi
                        for (Map.Entry<String,String> entry : Sets.difference(newMethods.keySet(), oldMethods).stream()
                                .map(fqn -> Map.entry(fqn, newMethods.get(fqn)))
                                .toList()) {
                            String fqn        = entry.getKey();
                            String simpleName = entry.getValue();
                            neo4jDAO.mergeMethod(owner, fqn, simpleName);
                            neo4jDAO.relateProjectToMethod(projectId, owner, fqn);
                        }

                        // 8) aggiorno tutte le CALLS per i metodi rimasti (intersezione)
                        Set<String> retained = Sets.intersection(oldMethods, newMethods.keySet());
                        for (String callerFqn : retained) {
                            // cancello vecchie relazioni CALLS
                            neo4jDAO.clearMethodCalls(owner, callerFqn);

                            // ristampo le chiamate dal nuovo AST
                            newCu.findAll(MethodDeclaration.class).stream()
                                    .filter(md -> md.resolve().getQualifiedSignature().equals(callerFqn))
                                    .flatMap(md -> md.findAll(MethodCallExpr.class).stream())
                                    .map(call -> call.resolve().getQualifiedSignature())
                                    .forEach(calleeFqn ->
                                            neo4jDAO.relateMethodsCall(owner, callerFqn, calleeFqn)
                                    );
                        }

                        filesModified++;
                        break;

                    case "DELETE":
                        // 1) verifica che il file esista
                        oldFile = mongoFileRepository.findByOwnerAndProjectNameAndPath(owner, projectName, path)
                                .orElseThrow(() -> new RuntimeException("File " + path + " not found"));
                        id = oldFile.getId();

                        // 2) estrazione metodi
                        CompilationUnit oldCuDel = StaticJavaParser.parse(oldFile.getContent());
                        Set<String> methodsToRemove = oldCuDel.findAll(MethodDeclaration.class).stream()
                                .map(md -> md.resolve().getQualifiedSignature())
                                .collect(Collectors.toSet());

                        // 3) eliminazione relazioni e metodi
                        for (String fqn : methodsToRemove) {
                            neo4jDAO.removeProjectToMethod(projectId, owner, fqn);
                            neo4jDAO.deleteMethodIfOrphan(owner, fqn);
                        }

                        // 4) elimino il file da Mongo e aggiorno i contatori
                        int removedLines = oldFile.getLines();
                        mongoFileRepository.deleteById(id);
                        linesDeleted += removedLines;
                        filesModified++;
                        break;

                    default:
                        throw new RuntimeException("Unsupported action: " + change.getAction());
                }
            }

            // 4) registra un nuovo Commit
            Commit commit = new Commit();
            commit.setFilesModified(filesModified);
            commit.setLinesAdded(linesAdded);
            commit.setLinesDeleted(linesDeleted);
            commit.setTimestamp(Instant.now());
            mongoCommitRepository.save(commit);

            session.commitTransaction();
            return ProjectDTO.fromProject(project);

        } catch (Exception e) {
            session.abortTransaction();
            throw new RuntimeException("Commit failed: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    private String extractProjectFromPackage(String pkg) {
        String[] parts = pkg.split("\\.");
        return parts.length >= 3 ? parts[2] : parts[1];
    }

    public PathDTO findVulnerabilityPath(String projectId) {
        // se non trova nulla, distanza -1 e lista vuota
        return neo4jDAO.getVulnerabilityPath(projectId)
                .orElse(new PathDTO(-1, List.of()));
    }

    public PathDTO findVulnerabilityPathByOwnerAndName(String owner, String projectName) {
        return neo4jDAO.getVulnerabilityPathByOwnerAndName(owner, projectName)
                .orElse(new PathDTO(-1, List.of()));
    }

    public List<NeoProjectDTO> discoverProjects(String username) {
        return neoProjectRepository.findRecommendedProjects(username);
    }

    public List<Method> getInefficienciesByProjectId(String username, Boolean isAdmin, String projectId) {
        if (!isAdmin && !neoProjectRepository.isCollaborator(username, projectId)) {
            throw new RuntimeException("Forbidden: you are not collaborator on project " + projectId);
        }
        return neoMethodRepository.findTop20ByProjectId(projectId);
    }

    public List<Method> getInefficienciesByOwnerAndName(String username,
                                                        Boolean isAdmin,
                                                        String owner,
                                                        String projectName) {
        if (!isAdmin && !neoProjectRepository.isCollaboratorByOwnerAndName(username, owner, projectName)) {
            throw new RuntimeException("Forbidden: you are not collaborator on project " + owner + "/" + projectName);
        }
        return neoMethodRepository.findTop20ByOwnerAndProjectName(owner, projectName);
    }

    // get first 100 files
    public List<String> getAllFiles(String owner, String projectName) {
        return mongoFileRepository.findTop100ByOwnerAndProjectNameOrderByPathAsc(owner, projectName)
                .orElseThrow()
                .stream()
                .map(File::getPath)
                .collect(Collectors.toList());
    }


    // get files paginated in pages of 50
    public List<String> getAllFilesPaginated(String owner,
                                             String projectName,
                                             int page) {
        Page<File> p = mongoFileRepository.findByOwnerAndProjectNameOrderByPathAsc(
                owner, projectName, PageRequest.of(page, 50)
        );
        return p.stream()
                .map(File::getPath)
                .collect(Collectors.toList());
    }

    public void updateCollaborators(String owner, String projectName, String authenticatedUsername, String username, Project project, boolean isAdmin) {
        // retrieve projectId
        project = mongoProjectRepository.findByOwnerAndName(owner,projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // delegate to next function
        updateCollaborators(project.getId(), authenticatedUsername, username, project, isAdmin);
    }

    public void updateCollaborators(String projectId, String authenticatedUsername, String username, Project project, boolean isAdmin) {
        ClientSession session = mongoClient.startSession();
        Boolean neo = false;
        try {
            session.startTransaction();

            // check if user exists
            if (!mongoUserRepository.findByUsername(username).isPresent()) {
                throw new RuntimeException("User not found");
            }

            // get project
            if (project == null) {
                project = mongoProjectRepository.findById(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found"));
            }

            // check if user is administrator or admin
            if (!isAdmin && !project.getAdministrators().contains(authenticatedUsername)) {
                throw new RuntimeException("You cannot edit this project");
            }

            // add user in administrators
            project.addAdministrator(username);

            // update project
            mongoProjectRepository.save(project);

            // add user to project
            neo4jDAO.addCollaborator(username, projectId);
            neo = true;

            session.commitTransaction();

        } catch (Exception e) {
            session.abortTransaction();
            if (neo) {
                throw new RuntimeException("Project creation succeded in Neo4j but failed in MongoDB, check for " +
                        "consistency in database");
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public void deleteCollaborators(String owner, String projectName, String authenticatedUsername, String username, Project project, boolean isAdmin) {
        // retrieve projectId
        project = mongoProjectRepository.findByOwnerAndName(owner,projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // delegate to next function
        deleteCollaborators(project.getId(), authenticatedUsername, username, project, isAdmin);
    }

    public void deleteCollaborators(String projectId, String authenticatedUsername, String username, Project project, boolean isAdmin) {
        ClientSession session = mongoClient.startSession();
        Boolean neo = false;
        try {
            session.startTransaction();

            // get project
            if (project == null) {
                project = mongoProjectRepository.findById(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found"));
            }

            // check if user is administrator or admin
            if (!project.getAdministrators().contains(authenticatedUsername) && !isAdmin) {
                throw new RuntimeException("You cannot edit this project");
            }

            // remove in administrators
            project.removeAdministrator(username);

            // update project
            mongoProjectRepository.save(project);

            // add user to project
            neo4jDAO.removeCollaborator(username, projectId);
            neo = true;

            session.commitTransaction();

        } catch (Exception e) {
            session.abortTransaction();
            if (neo) {
                throw new RuntimeException("Administrator removal succeded in Neo4j but failed in MongoDB, check for " +
                        "consistency in database");
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            session.close();
        }
    }

       
    /**
     * Analyze method complexity by counting assignments, arithmetic operations, and loops
     */
    private MethodMetrics analyzeMethodComplexity(MethodDeclaration method) {
        int assignmentCount = 0;
        int arithmeticCount = 0;
        int loopCount = 0;
        
        // Count assignments
        assignmentCount += method.findAll(com.github.javaparser.ast.expr.AssignExpr.class).size();
        assignmentCount += method.findAll(com.github.javaparser.ast.expr.VariableDeclarationExpr.class).size();
        
        // Count arithmetic operations
        arithmeticCount += method.findAll(com.github.javaparser.ast.expr.BinaryExpr.class)
            .stream()
            .mapToInt(expr -> {
                switch (expr.getOperator()) {
                    case PLUS, MINUS, MULTIPLY, DIVIDE, REMAINDER -> {
                        return 1;
                    }
                    default -> {
                        return 0;
                    }
                }
            })
            .sum();
        arithmeticCount += method.findAll(com.github.javaparser.ast.expr.UnaryExpr.class)
            .stream()
            .mapToInt(expr -> {
                switch (expr.getOperator()) {
                    case PLUS, MINUS, PREFIX_INCREMENT, PREFIX_DECREMENT, 
                         POSTFIX_INCREMENT, POSTFIX_DECREMENT -> {
                        return 1;
                    }
                    default -> {
                        return 0;
                    }
                }
            })
            .sum();
        
        // Count loops
        loopCount += method.findAll(com.github.javaparser.ast.stmt.ForStmt.class).size();
        loopCount += method.findAll(com.github.javaparser.ast.stmt.ForEachStmt.class).size();
        loopCount += method.findAll(com.github.javaparser.ast.stmt.WhileStmt.class).size();
        loopCount += method.findAll(com.github.javaparser.ast.stmt.DoStmt.class).size();
        
        return new MethodMetrics(assignmentCount, arithmeticCount, loopCount);
    }
    
    /**
     * Simple data class to hold method complexity metrics
     */
    private static class MethodMetrics {
        final int assignmentCount;
        final int arithmeticCount;
        final int loopCount;
        
        MethodMetrics(int assignmentCount, int arithmeticCount, int loopCount) {
            this.assignmentCount = assignmentCount;
            this.arithmeticCount = arithmeticCount;
            this.loopCount = loopCount;
        }
    }
}
