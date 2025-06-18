package it.unipi.githeritage.DAO.Neo4j;

import it.unipi.githeritage.DTO.PathDTO;
import it.unipi.githeritage.DTO.ProjectDTO;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class Neo4jDAO {
    private static final int PAGE_SIZE = 100;
    private final Neo4jClient client;

    public Neo4jDAO(Neo4jClient client) {
        this.client = client;
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

    public void mergeUser(String username) {
        client.query("MERGE (u:User {username: $username})")
                .bind(username).to("username")
                .run();
    }

    public void mergeProject(String projectId, String name) {
        client.query("MERGE (p:Project {id: $id, name: $name})")
                .bind(projectId).to("id")
                .bind(name).to("name")
                .run();
    }

    public void relateUserToProject(String username, String projectId) {
        client.query("""
            MATCH (u:User {username: $username}), (p:Project {id: $projectId})
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

    public void relateProjectDependency(String projectId, String depProjectId) {
        client.query("""
            MATCH (p1:Project {id: $projectId}), (p2:Project {id: $depProjectId})
            MERGE (p1)-[:DEPENDS_ON]->(p2)
            """)
                .bind(projectId).to("projectId")
                .bind(depProjectId).to("depProjectId")
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
        // garantisco l’esistenza dei due nodi
        mergeUser(followerUsername);
        mergeUser(followedUsername);
        client.query("""
            MATCH (f:User {username: $follower}), (t:User {username: $followed})
            MERGE (f)-[:FOLLOWS]->(t)
            """)
                .bind(followerUsername).to("follower")
                .bind(followedUsername).to("followed")
                .run();
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
                .map(row -> ((Number)row.get("dist")).intValue())
                .orElse(-1);
    }

    public Optional<PathDTO> getFollowPath(String userA, String userB) {
        String cypher = """
        MATCH (a:User {username: $userA}), (b:User {username: $userB})
        MATCH p = shortestPath((a)-[:FOLLOWS*]-(b))
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
        MATCH (a:User {username: $userA}), (b:User {username: $userB})
        MATCH p = shortestPath((a)-[:COLLABORATES_ON*]-(b))
        RETURN 
          toInteger(length(p)/2) AS dist,
          [n IN nodes(p) | 
             CASE 
               WHEN n:User THEN n.username 
               ELSE n.owner + '/' + n.name 
             END
          ] AS nodes
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


    public Optional<PathDTO> getVulnerabilityPath(String projectId) {
        String cypher = """
        MATCH (start:Project {id: $projectId})
        // cerchiamo il progetto vulnerabile più vicino
        MATCH path = shortestPath(
          (start)-[:DEPENDS_ON*]->(v:Project {vulnerability: true})
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
              (start)-[:DEPENDS_ON*]->(v:Project {vulnerability: true})
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

}
