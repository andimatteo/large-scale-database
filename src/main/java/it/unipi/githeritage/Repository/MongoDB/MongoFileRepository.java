package it.unipi.githeritage.Repository.MongoDB;

import it.unipi.githeritage.Model.MongoDB.File;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoFileRepository extends MongoRepository<File, String> {
    Optional<File> findById(String id);
    Optional<File> findByProjectIdAndPath(String projectId, String path);
}
