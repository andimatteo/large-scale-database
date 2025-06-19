package it.unipi.githeritage.Repository.MongoDB;

import it.unipi.githeritage.Model.MongoDB.Commit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoCommitRepository extends MongoRepository<Commit, String> {
}
