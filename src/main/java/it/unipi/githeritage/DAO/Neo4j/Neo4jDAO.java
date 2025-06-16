package it.unipi.githeritage.DAO.Neo4j;


import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import it.unipi.githeritage.DTO.ProjectDTO;
import it.unipi.githeritage.DTO.UserDTO;

import java.util.List;
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
            MATCH (p:Project {id: })-[:DEPENDS_ON]->(d:Project)
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
            MATCH (p:Project {id: })-[:DEPENDS_ON*]->(d:Project)
            RETURN DISTINCT d.id AS depId
            LIMIT 200
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("depId"))
                .collect(Collectors.toList());
    }

    public List<String> recursiveDependenciesPaginated(String projectId, int page) {
        String cypher = """
            MATCH (p:Project {id: })-[:DEPENDS_ON*1..]->(d:Project)
            RETURN DISTINCT d.id AS dependencyId
            SKIP 
            LIMIT 
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
            MATCH (p:Project {id: })-[:HAS_METHOD]->(m:Method)
            RETURN m.signature AS sig
            LIMIT 200
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("sig"))
                .collect(Collectors.toList());
    }

    public List<String> projectMethodsPaginated(String projectId, int page) {
        String cypher = """
            MATCH (p:Project {id: })-[:HAS_METHOD]->(m:Method)
            RETURN m.signature AS sig
            SKIP 
            LIMIT 200
            """;
        return client.query(cypher)
                .bind(projectId).to("projectId")
                .bind(page * PAGE_SIZE).to("skip")
                .fetch().all()
                .stream()
                .map(m -> (String) m.get("sig"))
                .collect(Collectors.toList());
    }

    public void addProject(ProjectDTO projectDTO) {
        String cypher = """
            CREATE (p:Project {id: $id, name: $name})
            """;
        
        client.query(cypher)
                .bind(projectDTO.getId()).to("id")
                .bind(projectDTO.getName()).to("name")
                .run();
        
        if (projectDTO.getOwner() != null) {
            String relCypher = """
                MATCH (p:Project {id: $projectId}), (u:User {username: $username})
                MERGE (u)-[:COLLABORATES_ON]->(p)
                """;
            System.out.println("Adding project ownership relationship for user: " + projectDTO);
            client.query(relCypher)
                    .bind(projectDTO.getId()).to("projectId")
                    .bind(projectDTO.getOwner()).to("username")
                    .run();
        }
    }

    // update project name
    public void updateProject(ProjectDTO projectDTO) {
        String cypher = """
            MATCH (p:Project {id: $id})
            SET p.name = $name
            """;
        
        client.query(cypher)
                .bind(projectDTO.getId()).to("id")
                .bind(projectDTO.getName()).to("name")
                .run();
    }
    
    public void addUser(UserDTO user) {
        String cypher = """
            CREATE (u:User {username: $username})
            """;
    
        client.query(cypher)
                .bind(user.getUsername()).to("username")
                .run();
    }

    public void followUser(String followerUsername, String followedUsername) {
        String cypher = """
            MATCH (follower:User {username: $followerUsername}), (followed:User {username: $followedUsername})
            MERGE (follower)-[:FOLLOWS]->(followed)
            """;
        
        client.query(cypher)
                .bind(followerUsername).to("followerUsername")
                .bind(followedUsername).to("followedUsername")
                .run();
    }

    public void unfollowUser(String followerUsername, String followedUsername) {
        String cypher = """
            MATCH (follower:User {username: $followerUsername})-[r:FOLLOWS]->(followed:User {username: $followedUsername})
            DELETE r
            """;
        
        client.query(cypher)
                .bind(followerUsername).to("followerUsername")
                .bind(followedUsername).to("followedUsername")
                .run();
    }
}
