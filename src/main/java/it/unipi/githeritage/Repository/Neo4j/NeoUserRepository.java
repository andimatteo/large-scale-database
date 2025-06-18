package it.unipi.githeritage.Repository.Neo4j;

import it.unipi.githeritage.Model.Neo4j.Project;
import it.unipi.githeritage.Model.Neo4j.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NeoUserRepository extends Neo4jRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query("MATCH (u:User {username: $username})<-[:FOLLOWS]-(f:User) RETURN f.username")
    List<String> findFollowersUsernamesByUsername(@Param("username") String username);

    @Query("MATCH (u:User {username: $username})-[:FOLLOWS]->(f:User) RETURN f.username")
    List<String> findFollowsUsernamesByUsername(@Param("username") String username);

    @Query("""
       MATCH (me:User {username: $username})-[:FOLLOWS]->(f:User)
       MATCH (f)-[:COLLABORATES_ON]->(p:Project)
       WHERE NOT (me)-[:COLLABORATES_ON]->(p)
       RETURN DISTINCT p
       LIMIT 20
    """)
    List<Project> findRecommendedProjects(String username);

    @Query("""
       MATCH (me:User {username: $username})-[:FOLLOWS]->(f:User)-[:FOLLOWS]->(ff:User)
       WHERE NOT (me)-[:FOLLOWS]->(ff)
         AND ff.username <> $username
       RETURN DISTINCT ff
       LIMIT 20
    """)
    List<User> findSuggestedPeople(String username);
}

