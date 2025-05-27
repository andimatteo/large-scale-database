package it.unipi.githeritage.repository.mongodb;

import it.unipi.githeritage.model.mongodb.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoProjectRepository extends MongoRepository<Project, String> {
}
