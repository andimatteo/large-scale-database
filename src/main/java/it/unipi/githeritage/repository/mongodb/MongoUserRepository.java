package it.unipi.githeritage.repository.mongodb;

import it.unipi.githeritage.model.mongodb.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoUserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
