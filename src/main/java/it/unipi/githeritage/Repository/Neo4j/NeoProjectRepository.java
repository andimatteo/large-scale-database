package it.unipi.githeritage.Repository.Neo4j;

import it.unipi.githeritage.Model.Neo4j.Project;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface NeoProjectRepository extends Neo4jRepository<Project, String> {
    Optional<Project> findByName(String name);
}

