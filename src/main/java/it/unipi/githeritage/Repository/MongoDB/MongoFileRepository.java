package it.unipi.githeritage.Repository.MongoDB;

import it.unipi.githeritage.Model.MongoDB.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoFileRepository extends MongoRepository<File, String> {
    Optional<File> findByOwnerAndProjectNameAndPath(String owner, String projectName, String path);
    Optional<List<File>> findTop100ByOwnerAndProjectNameOrderByPathAsc(String owner, String projectName);
    Page<File> findByOwnerAndProjectNameOrderByPathAsc(String owner, String projectName, Pageable pageable);
    void deleteByOwnerAndProjectName(String owner, String projectName);
    String id(String id);
}
