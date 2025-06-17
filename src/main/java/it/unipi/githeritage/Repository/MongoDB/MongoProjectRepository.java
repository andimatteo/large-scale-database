package it.unipi.githeritage.Repository.MongoDB;

import it.unipi.githeritage.DTO.ProjectDTO;
import it.unipi.githeritage.Model.MongoDB.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoProjectRepository extends MongoRepository<Project, String> {
    Optional<ProjectDTO> findByOwnerAndName(String username, String projectName);
}
