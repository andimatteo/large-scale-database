package it.unipi.githeritage.Repository.Neo4j;

import it.unipi.githeritage.Model.Neo4j.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NeoUserRepository extends Neo4jRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query("MATCH (u:User {username: $username})<-[:FOLLOWS]-(f:User) RETURN f.username")
    List<String> findFollowersUsernamesByUsername(@Param("username") String username);

    @Query("MATCH (u:User {username: $username})-[:FOLLOWS]->(f:User) RETURN f.username")
    List<String> findFollowsUsernamesByUsername(@Param("username") String username);
}

