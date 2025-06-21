package it.unipi.githeritage.DAO.Neo4j;

import it.unipi.githeritage.DTO.PathDTO;
import it.unipi.githeritage.DTO.ProjectDTO;
import it.unipi.githeritage.Model.Neo4j.Project;
import org.checkerframework.checker.units.qual.A;
import org.neo4j.driver.summary.ResultSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class Neo4jDAO {
    private static final int PAGE_SIZE = 100;
    private final Neo4jClient client;

    @Autowired
    private Neo4jTemplate template;

    public Neo4jDAO(Neo4jClient client) {
        this.client = client;
    }

    /**
     * Creates a unique constraint on the username attribute of User nodes
     * to improve query performance and ensure username uniqueness
     */
    public void createUsernameIndex() {
        client.query("CREATE CONSTRAINT IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE")
              .run();
    }

    /**
     * Creates a unique constraint on the id attribute of Project nodes
     * to improve query performance and ensure project id uniqueness
     */
    public void createProjectIdIndex() {
        client.query("CREATE CONSTRAINT IF NOT EXISTS FOR (p:Project) REQUIRE p.id IS UNIQUE")
              .run();
        client.query("CREATE CONSTRAINT IF NOT EXISTS FOR (m:Method) REQUIRE m.fullyQualifiedName IS UNIQUE")
              .run();
    }



    public List<String> firstLevelDependencies(String projectId) {
        String cypher = """
            MATCH (p:Project {id: $projectId})-[:DEPENDS_ON]->(d:Project)
            RETURN d.id AS depId
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("depId"))
                .collect(Collectors.toList());
    }

    public List<String> firstLevelDependencies(String owner, String projectName) {
        String cypher = """
            MATCH (p:Project {owner: $owner, name: $projectName})-[:DEPENDS_ON]->(d:Project)
            RETURN d.id AS depId
            """;
        return client.query(cypher)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("depId"))
                .collect(Collectors.toList());
    }

    public List<String> recursiveDependencies(String projectId) {
        String cypher = """
            MATCH (p:Project {id: $projectId})-[:DEPENDS_ON*]->(d:Project)
            RETURN DISTINCT d.id AS depId
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .bind(200).to("limit")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("depId"))
                .collect(Collectors.toList());
    }

    public List<String> recursiveDependencies(String owner, String projectName) {
        String cypher = """
            MATCH (p:Project {owner: $owner, name: $projectName})-[:DEPENDS_ON*]->(d:Project)
            RETURN DISTINCT d.id AS depId
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .bind(200).to("limit")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("depId"))
                .collect(Collectors.toList());
    }

    public List<String> recursiveDependenciesPaginated(String projectId, int page) {
        String cypher = """
            MATCH (p:Project {id: $projectId})-[:DEPENDS_ON*1..]->(d:Project)
            RETURN DISTINCT d.id AS dependencyId
            SKIP $skip
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .bind(page * PAGE_SIZE).to("skip")
                .bind(PAGE_SIZE).to("limit")
                .fetch().all()
                .stream()
                .map(row -> (String) row.get("dependencyId"))
                .collect(Collectors.toList());
    }

    public List<String> recursiveDependenciesPaginated(String owner, String projectName, int page) {
        String cypher = """
            MATCH (p:Project {owner: $owner, name: $projectName})-[:DEPENDS_ON*1..]->(d:Project)
            RETURN DISTINCT d.id AS dependencyId
            SKIP $skip
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .bind(page * PAGE_SIZE).to("skip")
                .bind(PAGE_SIZE).to("limit")
                .fetch().all()
                .stream()
                .map(row -> (String) row.get("dependencyId"))
                .collect(Collectors.toList());
    }

    public List<String> projectMethods(String projectId) {
        String cypher = """
            MATCH (p:Project {id: $projectId})-[:HAS_METHOD]->(m:Method)
            RETURN m.fullyQualifiedName AS sig
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .bind(200).to("limit")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("sig"))
                .collect(Collectors.toList());
    }

    public List<String> projectMethods(String owner, String projectName) {
        String cypher = """
            MATCH (p:Project {owner: $owner, name: $projectName})-[:HAS_METHOD]->(m:Method)
            RETURN m.fullyQualifiedName AS sig
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .bind(200).to("limit")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("sig"))
                .collect(Collectors.toList());
    }

    public List<String> projectMethodsPaginated(String projectId, int page) {
        String cypher = """
            MATCH (p:Project {id: $projectId})-[:HAS_METHOD]->(m:Method)
            RETURN m.fullyQualifiedName AS sig
            SKIP $skip
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .bind(page * PAGE_SIZE).to("skip")
                .bind(PAGE_SIZE).to("limit")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("sig"))
                .collect(Collectors.toList());
    }

    public List<String> projectMethodsPaginated(String owner, String projectName, int page) {
        String cypher = """
            MATCH (p:Project {owner: $owner, name: $projectName})-[:HAS_METHOD]->(m:Method)
            RETURN m.fullyQualifiedName AS sig
            SKIP $skip
            LIMIT $limit
            """;
        return client.query(cypher)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .bind(page * PAGE_SIZE).to("skip")
                .bind(PAGE_SIZE).to("limit")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("sig"))
                .collect(Collectors.toList());
    }

    public void mergeUser(String username) {
        client.query("MERGE (u:User {username: $username})")
                .bind(username).to("username")
                .run();
    }

    /**
     * Efficiently merges multiple users at once using UNWIND
     *
     * @param usernames List of usernames to merge
     */
    public void mergeUsers(List<String> usernames) {
        client.query("UNWIND $usernames AS username MERGE (u:User {username: username})")
                .bind(usernames).to("usernames")
                .run();
    }

    public void closeIdleConnections() {
        try {
            // Force immediate cleanup of idle connections
            Runtime.getRuntime().gc();
            Thread.sleep(50); // Brief pause to allow cleanup
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void mergeProject(String projectId, String name) {
        client.query("MERGE (p:Project {id: $id, name: $name})")
                .bind(projectId).to("id")
                .bind(name).to("name")
                .bind(owner).to("owner")
                .run();
    }

    /**
     * Efficiently merges multiple projects at once using UNWIND
     *
     * @param projects List of maps containing project attributes (id, owner, projectName, administrators, packageName)
     */
    public void mergeProjects(List<Map<String, Object>> projects) {
        client.query("""
            UNWIND $projects AS proj
            MERGE (p:Project {id: proj.id})
            ON CREATE SET p.owner = proj.owner, p.projectName = proj.projectName, p.packageName = proj.packageName
            ON MATCH SET p.owner = proj.owner, p.projectName = proj.projectName, p.packageName = proj.packageName
            WITH p, proj
            
            // Then create COLLABORATES_ON relationships for all administrators
            WITH p, proj
            UNWIND proj.administrators AS admin
            MATCH (u:User {username: admin})
            MERGE (u)-[:COLLABORATES_ON]->(p)
            """)
            .bind(projects).to("projects")
            .run();
    }

    public void relateUserToProject(String username, String projectId) {
        client.query("""
            MATCH (u:User {username: $username})
            MATCH (p:Project {id: $projectId})
            MERGE (u)-[:COLLABORATES_ON]->(p)
            """)
                .bind(username).to("username")
                .bind(projectId).to("projectId")
                .run();
    }

    public void mergeMethod(String owner, String fqn, String methodName) {
        client.query("""
            MERGE (m:Method {owner: $owner, fullyQualifiedName: $fqn})
            ON CREATE SET m.methodName = $methodName
            """)
                .bind(owner).to("owner")
                .bind(fqn).to("fqn")
                .bind(methodName).to("methodName")
                .run();
    }

    public void relateProjectToMethod(String projectId, String owner, String fqn) {
        client.query("""
            MATCH (p:Project {id: $projectId}), (m:Method {owner: $owner, fullyQualifiedName: $fqn})
            MERGE (p)-[:HAS_METHOD]->(m)
            """)
                .bind(projectId).to("projectId")
                .bind(owner).to("owner")
                .bind(fqn).to("fqn")
                .run();
    }

    public void removeProjectToMethod(String projectId, String owner, String fqn) {
        client.query("""
        MATCH (p:Project {id: $projectId})-[r:HAS_METHOD]->(m:Method {owner: $owner, fullyQualifiedName: $fqn})
        DELETE r
        """)
                .bind(projectId).to("projectId")
                .bind(owner).to("owner")
                .bind(fqn).to("fqn")
                .run();
    }

    public void relateMethodsCall(String owner, String callerFqn, String calleeFqn) {
        client.query("""
            MATCH (m1:Method {owner: $owner, fullyQualifiedName: $caller}),
                  (m2:Method {owner: $owner, fullyQualifiedName: $callee})
            MERGE (m1)-[:CALLS]->(m2)
            """)
                .bind(owner).to("owner")
                .bind(callerFqn).to("caller")
                .bind(calleeFqn).to("callee")
                .run();
    }

    public void clearMethodCalls(String owner, String callerFqn) {
        client.query("""
        MATCH (m:Method {owner: $owner, fullyQualifiedName: $caller})-[r:CALLS]->()
        DELETE r
        """)
                .bind(owner).to("owner")
                .bind(callerFqn).to("caller")
                .run();
    }

    public void deleteMethodIfOrphan(String owner, String fqn) {
        client.query("""
        MATCH (m:Method {owner: $owner, fullyQualifiedName: $fqn})
        WHERE NOT (m)<-[:HAS_METHOD]-()
          AND NOT (m)-[:CALLS]->()
          AND NOT (m)<-[:CALLS]-()
        DETACH DELETE m
        """)
                .bind(owner).to("owner")
                .bind(fqn).to("fqn")
                .run();
    }

    public void deleteMethodById(List<String> methodIds) {

    }

    public void relateProjectDependency(String projectId, String depProjectId) {
        client.query("""
            MATCH (p1:Project {id: $projectId}), (p2:Project {id: $depProjectId})
            MERGE (p1)-[:DEPENDS_ON]->(p2)
            """)
                .bind(projectId).to("projectId")
                .bind(depProjectId).to("depProjectId")
                .run();
    }

    public void relateProjectDependencyByPackageName(String projectId, String packageName) {
        client.query("""
            MERGE (p:Project {id: $projectId})
            MERGE (pkg:Project {packageName: $packageName})
            MERGE (p)-[:DEPENDS_ON]->(pkg)
            """)
                .bind(projectId).to("projectId")
                .bind(packageName).to("packageName")
                .run();
    }

    public void addProject(ProjectDTO projectDTO) {
        mergeProject(projectDTO.getId(), projectDTO.getName());
        if (projectDTO.getOwner() != null) {
            mergeUser(projectDTO.getOwner());
            relateUserToProject(projectDTO.getOwner(), projectDTO.getId());
        }
    }

    public void deleteProjectNodeById(String projectId) {
        client.query("""
        MATCH (p:Project {id: $projectId})
        DETACH DELETE p
        """)
                .bind(projectId).to("projectId")
                .run();
    }

    public void deleteProjectNodeByOwnerAndName(String owner, String projectName) {
        client.query("""
        MATCH (p:Project)
        WHERE p.owner = $owner AND p.name = $projectName
        DETACH DELETE p
        """)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .run();
    }

    public List<String> projectMethodsById(String projectId) {
        String cypher = """
        MATCH (p:Project {id: $projectId})-[:HAS_METHOD]->(m:Method)
        RETURN m.fullyQualifiedName AS sig
        """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .fetch().all()
                .stream()
                .map(m -> (String)m.get("sig"))
                .collect(Collectors.toList());
    }

    public List<String> projectMethodsByOwnerAndName(String owner, String projectName) {
        String cypher = """
        MATCH (p:Project)
        WHERE p.owner = $owner AND p.name = $projectName
        MATCH (p)-[:HAS_METHOD]->(m:Method)
        RETURN m.fullyQualifiedName AS sig
        """;
        return client.query(cypher)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .fetch().all()
                .stream()
                .map(m -> (String)m.get("sig"))
                .collect(Collectors.toList());
    }

    public void followUser(String followerUsername, String followedUsername) {
        try {
            boolean followedExists = client.query("""
            MATCH (t:User {username: $followed})
            RETURN true
            """)
                    .bind(followedUsername).to("followed")
                    .fetchAs(Boolean.class)
                    .one()               // Optional<Boolean>
                    .orElse(false);      // false se Optional.empty()

            if (!followedExists) {
                throw new RuntimeException("User '" + followedUsername + "' does not exist");
            }

            client.query("""
                MATCH (f:User {username: $follower})
                MATCH (t:User {username: $followed})
                MERGE (f)-[:FOLLOWS]->(t)
                """)
                    .bind(followerUsername).to("follower")
                    .bind(followedUsername).to("followed")
                    .run();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void unfollowUser(String followerUsername, String followedUsername) {
        client.query("""
            MATCH (f:User {username: $follower})-[r:FOLLOWS]->(t:User {username: $followed})
            DELETE r
            """)
                .bind(followerUsername).to("follower")
                .bind(followedUsername).to("followed")
                .run();
    }

    /**
     * Efficiently creates multiple follow relationships at once using UNWIND
     *
     * @param followRelationships List of maps containing follower and followed usernames
     */
    public void followUsers(List<Map<String, String>> followRelationships) {
        try {
            client.query("""
                UNWIND $relationships AS rel
                MATCH (follower:User {username: rel.follower}), (followed:User {username: rel.followed})
                MERGE (follower)-[:FOLLOWS]->(followed)
                """)
                    .bind(followRelationships).to("relationships")
                    .run();
        } finally {
            closeIdleConnections();
        }
    }

    /**
     * Distanza “follow” tra due utenti, considerando
     * la relazione FOLLOWS come non direzionale:
     * MATCH p = shortestPath((a)-[:FOLLOWS*]-(b))
     */
    public int followDistance(String userA, String userB) {
        String cypher = """
            MATCH (a:User {username: $userA}), (b:User {username: $userB})
            OPTIONAL MATCH p = shortestPath((a)-[:FOLLOWS*]-(b))
            RETURN CASE WHEN p IS NULL THEN -1 ELSE length(p) END AS dist
            """;
        return client.query(cypher)
                .bind(userA).to("userA")
                .bind(userB).to("userB")
                .fetch().one()
                .map(row -> ((Number) row.get("dist")).intValue())
                .orElse(-1);
    }

    public Optional<PathDTO> getFollowPath(String userA, String userB) {
        String cypher = """
        MATCH (a:User {username: $userA})
        MATCH (b:User {username: $userB})
        MATCH p = shortestPath((a)-[:FOLLOWS*1..8]-(b))
        RETURN 
          length(p)       AS dist,
          [n IN nodes(p) | n.username] AS nodes
        """;
        return client.query(cypher)
                .bind(userA).to("userA")
                .bind(userB).to("userB")
                .fetch().one()
                .map(record -> {
                    int dist = ((Number) record.get("dist")).intValue();
                    @SuppressWarnings("unchecked")
                    List<String> path = (List<String>) record.get("nodes");
                    return new PathDTO(dist, path);
                });
    }

    public Optional<PathDTO> getProjectPath(String userA, String userB) {

        String cypher = """
        MATCH (a:User {username: $userA})
        MATCH (b:User {username: $userB})
        MATCH p = shortestPath( (a)-[:COLLABORATES_ON*1..9]-(b) )
        WHERE p IS NOT NULL          // <— evita la riga “tutta null”
        WITH nodes(p) AS n, length(p) AS len
        RETURN
            toInteger(len / 2) AS distance,
            [ i IN range(0, size(n)-1) |
                CASE
                    WHEN n[i]:User
                         THEN 'USER: '    + coalesce(n[i].username,'<unknown>')
                    ELSE 'PROJECT: ' + coalesce(n[i].owner,'<no-owner>')
                                       + '/' +
                                       coalesce(n[i].name , coalesce(n[i].id,'<no-name>'))
                END
            ] AS nodes
        """;

        return client.query(cypher)
                .bind(userA).to("userA")
                .bind(userB).to("userB")
                .fetch()
                .one()                   // Optional<Map<String,Object>>
                .map(rec -> {
                    int dist = ((Number) rec.get("distance")).intValue();
                    @SuppressWarnings("unchecked")
                    List<String> seq = (List<String>) rec.get("nodes");
                    return new PathDTO(dist, seq);
                });
    }



    public Optional<PathDTO> getVulnerabilityPath(String projectId) {
        String cypher = """
        MATCH (start:Project {id: $projectId})
        // cerchiamo il progetto vulnerabile più vicino
        MATCH path = shortestPath(
          (start)-[:DEPENDS_ON*1..20]->(v:Project {vulnerability: true})
        )
        RETURN 
          length(path) AS dist,
          [n IN nodes(path) | 
             CASE 
               WHEN n:Project THEN n.owner + '/' + n.name 
               ELSE n.id 
             END
          ] AS nodes
        """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .fetch().one()
                .map(record -> {
                    int dist = ((Number)record.get("dist")).intValue();
                    @SuppressWarnings("unchecked")
                    List<String> nodes = (List<String>)record.get("nodes");
                    return new PathDTO(dist, nodes);
                });
    }

    public Optional<PathDTO> getVulnerabilityPathByOwnerAndName(String owner, String projectName) {
        String cypher = """
            MATCH (start:Project {owner: $owner, name: $projectName})
            MATCH path = shortestPath(
              (start)-[:DEPENDS_ON*1..20]->(v:Project {vulnerability: true})
            )
            RETURN 
              length(path) AS dist,
              [n IN nodes(path) | n.owner + '/' + n.name] AS nodes
            """;
        return client.query(cypher)
                .bind(owner).to("owner")
                .bind(projectName).to("projectName")
                .fetch().one()
                .map(record -> {
                    int dist = ((Number)record.get("dist")).intValue();
                    @SuppressWarnings("unchecked")
                    List<String> nodes = (List<String>)record.get("nodes");
                    return new PathDTO(dist, nodes);
                });
    }

    public void addCollaborator(String username, String projectId) {
        client.query("""
        MATCH (u:User {username: $username})
        MATCH (p:Project {id: $projectId})
        MERGE (u)-[:COLLABORATES_ON]->(p)
        """)
                .bind(username).to("username")
                .bind(projectId).to("projectId")
                .run();
    }

    public void removeCollaborator(String username, String projectId) {
        client.query("""
        MATCH (u:User {username: $username})-[r:COLLABORATES_ON]->(p:Project {id: $projectId})
        DELETE r
        """)
                .bind(username).to("username")
                .bind(projectId).to("projectId")
                .run();
    }
    public int countFollower(String username) {
        String cypher = """
            MATCH (:User)-[r:FOLLOWS]->(u:User {username: $username})
            RETURN COUNT(r) AS followerCount
            """;
        return client.query(cypher)
                .bind(username).to("username")
                .fetch().one()
                .map(row -> ((Number)row.get("followerCount")).intValue())
                .orElse(0);
    }

    public int countFollowing(String username) {
        String cypher = """
            MATCH (u:User {username: $username})-[r:FOLLOWS]->(:User)
            RETURN COUNT(r) AS followingCount
            """;
        return client.query(cypher)
                .bind(username).to("username")
                .fetch().one()
                .map(row -> ((Number)row.get("followingCount")).intValue())
                .orElse(0);
    }
}
