package it.unipi.githeritage.repository.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import it.unipi.githeritage.model.neo4j.User;

import java.util.List;

public interface Neo4jUserRepository extends Neo4jRepository<User, String> {

}
