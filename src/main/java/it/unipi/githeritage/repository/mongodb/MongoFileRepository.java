package it.unipi.githeritage.repository.mongodb;

import it.unipi.githeritage.model.mongodb.File;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoFileRepository extends MongoRepository<File, String> {
    List<File> findByFilename(String filename);
}
