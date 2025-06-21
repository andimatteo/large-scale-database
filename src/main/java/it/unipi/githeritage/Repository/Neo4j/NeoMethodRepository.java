package it.unipi.githeritage.Repository.Neo4j;

import it.unipi.githeritage.Model.Neo4j.Method;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface NeoMethodRepository extends Neo4jRepository<Method, String> {
    /** Prende i top-20 Method di un progetto (id) ordinati per hotness desc */
    @Query("""
       MATCH (p:Project {id: $projectId})-[:HAS_METHOD]->(m:Method)
       RETURN m
       ORDER BY m.hotness DESC
       LIMIT 20
    """)
    List<Method> findTop20ByProjectId(String projectId);

    /** Prende i top-20 Method di un progetto (owner+projectName) ordinati per hotness desc */
    @Query("""
       MATCH (p:Project {owner: $owner, name: $projectName})-[:HAS_METHOD]->(m:Method)
       RETURN m
       ORDER BY m.hotness DESC
       LIMIT 20
    """)
    List<Method> findTop20ByOwnerAndProjectName(String owner, String projectName);
}
