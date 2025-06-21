package it.unipi.githeritage.Repository.Neo4j;

import it.unipi.githeritage.DTO.NeoProjectDTO;
import it.unipi.githeritage.Model.Neo4j.Project;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NeoProjectRepository extends Neo4jRepository<Project, String> {
    @Query("""
        MATCH (me:User {username:$username})-[:FOLLOWS]-(:User)-[:COLLABORATES_ON]-(p:Project)
        WHERE NOT (me)-[:COLLABORATES_ON]-(p)
        RETURN DISTINCT p
        LIMIT 20
        """)
    List<NeoProjectDTO> findRecommendedProjects(@Param("username") String username);

    /** Controlla se user collabora sul progetto (id) */
    @Query("""
       MATCH (u:User {username: $username})-[:COLLABORATES_ON]->(p:Project {id: $projectId})
       RETURN COUNT(p)>0
    """)
    boolean isCollaborator(String username, String projectId);

    /** Controlla se user collabora sul progetto (owner+projectName) */
    @Query("""
       MATCH (u:User {username: $username})-[:COLLABORATES_ON]->(p:Project {owner: $owner, name: $projectName})
       RETURN COUNT(p)>0
    """)
    boolean isCollaboratorByOwnerAndName(String username, String owner, String projectName);
}

