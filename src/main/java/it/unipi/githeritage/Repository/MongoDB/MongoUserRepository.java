package it.unipi.githeritage.Repository.MongoDB;

import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.Model.MongoDB.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoUserRepository extends MongoRepository<User, String> {
    UserDTO findByUsername(String username);
    UserDTO deleteByUsername(String username);
}
