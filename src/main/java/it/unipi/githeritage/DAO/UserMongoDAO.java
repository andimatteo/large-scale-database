package it.unipi.githeritage.DAO;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.model.mongodb.User;
import org.bson.Document;
import org.springframework.stereotype.Component;

import it.unipi.githeritage.repository.mongodb.MongoUserRepository;

@Component
public class UserMongoDAO {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private MongoUserRepository repo;

    //@Autowired
    //private final MongoTemplate mongoTemplate;

    public User addUser(User user) {

        return repo.save(user);
        
    }

    public User addUser(UserDTO user) {
        // Convert UserDTO to User
        User newUser = User.fromDTO(user);
        return repo.save(newUser);
        
    }

    public void deleteUser(String userId) {
        repo.deleteById(userId);
    }
    
}