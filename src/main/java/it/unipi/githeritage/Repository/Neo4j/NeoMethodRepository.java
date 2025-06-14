package it.unipi.githeritage.Repository.Neo4j;

import it.unipi.githeritage.Model.Neo4j.Method;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface NeoMethodRepository extends Neo4jRepository<Method, String> {
    Optional<Method> findByName(String name);
}
