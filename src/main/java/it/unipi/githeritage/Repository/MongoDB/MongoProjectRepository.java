package it.unipi.githeritage.Repository.MongoDB;

import it.unipi.githeritage.Model.MongoDB.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findById(int id); // usa 'id' campo logico, non MongoDB _id
}
