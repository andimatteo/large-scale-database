package it.unipi.githeritage.repository.mongodb;

import it.unipi.githeritage.model.mongodb.Project;
import it.unipi.githeritage.model.mongodb.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoUserRepository extends MongoRepository<User, String> {
}
