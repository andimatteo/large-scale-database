package it.unipi.githeritage.DAO.MongoDB;

import com.mongodb.client.MongoClient;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.bcrypt.BCrypt;

import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.Model.MongoDB.User;
import org.bson.Document;

import it.unipi.githeritage.Repository.MongoDB.MongoUserRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserMongoDAO {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private MongoUserRepository repo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoUserRepository mongoUserRepository;

    public User addUser(User user) {
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        return repo.save(user);
        
    }

    public User addUser(UserDTO user) {
        // Convert UserDTO to User

        User newUser = User.fromDTO(user);
        return addUser(newUser);
        
    }

    public User editUser(User user) {
        Update update = new Update();
        if (user.getPassword() != null) {
            // generate hash from password with bcrypt
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            update.set("password",hashedPassword);
        }
        if (user.getEmail() != null) {
            update.set("email", user.getEmail());
        }
        if (user.getName() != null) {
            update.set("fullName", user.getName());
        }
        if (user.getSurname() != null) {
            update.set("surname", user.getSurname());
        }
        if (user.getNationality() != null) {
            update.set("nationality", user.getNationality());
        }
        // if (user.getFollowerNumber() != null) {
        //     update.set("followerNumber", user.getFollowerNumber());
        // }
        // if (user.getFollowingNumber() != null) {
        //     update.set("followingNumber", user.getFollowingNumber());
        // }
        // if (user.getComments() != null) {
        //     update.set("comments", user.getComments());
        // }
        // if (user.getProjects() != null) {
        //     update.set("projects", user.getProjects());
        // }
        if (user.getIsAdmin() != null) {
            update.set("isAdmin", user.getIsAdmin());
        }

        if (!update.getUpdateObject().isEmpty()) {
            Query query = new Query(Criteria.where("username").is(user.getUsername()));
            mongoTemplate.updateFirst(query, update, User.class);
        }
        // Reload the updated user from the database
        User updatedUser = repo.findById(user.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found: " + user.getUsername()));

        return updatedUser;
        
    }

    public User editUser(UserDTO user) {
        // Convert UserDTO to User
        User newUser = User.fromDTO(user);
        return editUser(newUser);
    }


    public void deleteUser(String userId) {
        repo.deleteById(userId);
    }

    public UserDTO getUserByUsername(String username) {
        // TODO: Implement the logic to retrieve a user by username from MongoDB
        // Example placeholder:
        // search by id username
        User found = repo.findById(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return UserDTO.fromUser(found);
    }

    public List<Document> aggregateUsers(List<Bson> pipeline) {
        // se ti serve l’AggregateIterable di MongoDriver, prendi la collection grezza:
        return mongoTemplate.getCollection("user")
                .aggregate(pipeline)
                .into(new ArrayList<>());
    }
    
}