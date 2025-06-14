package it.unipi.githeritage.DAO.Neo4j;


import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class Neo4jDAO {
    private static final int PAGE_SIZE = 100;
    private Neo4jClient client;

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
            MATCH (p:Project {id: $projectId})-[:HAS_METHOD]->(m:Method)
            RETURN m.signature AS sig
            SKIP $skip
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


}
