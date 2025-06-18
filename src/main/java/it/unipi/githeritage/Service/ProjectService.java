package it.unipi.githeritage.Service;

import com.google.common.collect.Sets;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import it.unipi.githeritage.DAO.MongoDB.FileMongoDAO;
import it.unipi.githeritage.DAO.MongoDB.ProjectMongoDAO;
import it.unipi.githeritage.DAO.MongoDB.UserMongoDAO;
import it.unipi.githeritage.DAO.Neo4j.Neo4jDAO;
import it.unipi.githeritage.DTO.*;
import it.unipi.githeritage.Model.MongoDB.Commit;
import it.unipi.githeritage.Model.MongoDB.File;
import it.unipi.githeritage.Model.MongoDB.Project;
import it.unipi.githeritage.Model.Neo4j.Method;
import it.unipi.githeritage.Repository.MongoDB.MongoCommitRepository;
import it.unipi.githeritage.Repository.MongoDB.MongoFileRepository;
import it.unipi.githeritage.Repository.MongoDB.MongoProjectRepository;
import it.unipi.githeritage.Repository.Neo4j.NeoMethodRepository;
import it.unipi.githeritage.Repository.Neo4j.NeoProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
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

    @Autowired
    private MongoFileRepository mongoFileRepository;

    @Autowired
    private MongoClient mongo;

    @Autowired
    private MongoCommitRepository mongoCommitRepository;

    @Autowired
    private NeoProjectRepository neoProjectRepository;

    @Autowired
    private NeoMethodRepository neoMethodRepository;

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

    // todo rimuovere i commits dal risultato della query (projection)
    public ProjectDTO getProjectById(String id) {
        Optional<Project> opt = mongoProjectRepository.findById(id);
        return opt.isPresent() ? opt.get().toDTO(opt.get()) : null; // TODO controllare che abbia senso
    }

    // todo rimuovere i commits dal risultato della query (projection)
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

    public ProjectDTO deleteProject(String projectId, String authenticatedUser) {
        // 1) prendo document da Mongo
        Project project = mongoProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        // delego all’altro overload, passandogli owner+name
        return deleteProject(
                project.getOwner(),
                project.getName(),
                projectId,
                authenticatedUser
        );
    }

    public ProjectDTO deleteProject(String owner,
                                    String projectName,
                                    String authenticatedUser) {
        // cerco in Mongo con owner+name
        ProjectDTO project = mongoProjectRepository
                .findByOwnerAndName(owner, projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        // delego all’altro overload
        return deleteProject(
                owner,
                projectName,
                project.getId(),
                authenticatedUser
        );
    }

    private ProjectDTO deleteProject(String owner,
                                     String projectName,
                                     String projectId,
                                     String authenticatedUser) {
        // 1) permessi
        if (!mongoProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"))
                .getAdministrators().contains(authenticatedUser)) {
            throw new RuntimeException("Forbidden");
        }

        // 2) Mongo: cancello tutti i file e il progetto
        mongoFileRepository.deleteByOwnerAndProjectName(owner, projectName);
        mongoProjectRepository.deleteById(projectId);

        // 3) Neo4j: prelevo tutti i metodi (usando id)
        List<String> fqnList = neo4jDAO.projectMethodsById(projectId);

        // 4) rimuovo il nodo Project (e tutte le sue relazioni)
        neo4jDAO.deleteProjectNodeById(projectId);

        // 5) pulisco i Method rimasti
        for (String fqn : fqnList) {
            neo4jDAO.clearMethodCalls(owner, fqn);
            neo4jDAO.deleteMethodIfOrphan(owner, fqn);
        }

        // 6) restituisco conferma
        ProjectDTO dto = new ProjectDTO();
        dto.setId(projectId);
        dto.setName(projectName);
        dto.setOwner(owner);
        return dto;
    }

    // todo finish update project
    public Project updateProject(CommitIdDTO commitIdDTO, String username) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();

            String projectId = commitIdDTO.getProjectId();
            // 1) prendi il progetto e controlla che esista e che l’utente sia admin
            Project project = mongoProjectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            String owner = project.getOwner();
            String projectName = project.getName();

            if (!project.getAdministrators().contains(username)) {
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
                        neo4jDAO.mergeProject(projectId, projectName);
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
            commit.setAuthor(username);
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
            ProjectDTO project = mongoProjectRepository.findByOwnerAndName(owner, projectName)
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
                        neo4jDAO.mergeProject(projectId, projectName);
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
                                .collect(Collectors.toList())) {
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
            commit.setAuthor(username);
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

    public List<it.unipi.githeritage.Model.Neo4j.Project> discoverProjects(String username) {
        return neoProjectRepository.findRecommendedProjects(username);
    }

    public List<Method> getInefficienciesByProjectId(String username, String projectId) {
        if (!neoProjectRepository.isCollaborator(username, projectId)) {
            throw new RuntimeException("Forbidden: you are not collaborator on project " + projectId);
        }
        return neoMethodRepository.findTop20ByProjectId(projectId);
    }

    public List<Method> getInefficienciesByOwnerAndName(String username,
                                                        String owner,
                                                        String projectName) {
        if (!neoProjectRepository.isCollaboratorByOwnerAndName(username, owner, projectName)) {
            throw new RuntimeException("Forbidden: you are not collaborator on project " + owner + "/" + projectName);
        }
        return neoMethodRepository.findTop20ByOwnerAndProjectName(owner, projectName);
    }

}
