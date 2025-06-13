package it.unipi.githeritage.service.mongodb;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import it.unipi.githeritage.DAO.UserMongoDAO;
import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.model.mongodb.User;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private final MongoClient mongoClient;

    @Autowired
    private UserMongoDAO userMongoDAO;

    public UserDTO addUser(UserDTO userDTO) {
        ClientSession session = mongoClient.startSession();
        Boolean neo4j = false;
        try {
            System.out.println("Starting transaction to add user: " + userDTO.getUsername());
            session.startTransaction();
            // Save the user in MongoDB
            userDTO.setIsAdmin(false); // Default to non-admin
            //userDTO.setRegistrationDate(LocalDate.now()); // Set registration date to current date

            userMongoDAO.addUser(userDTO);
            // Save the user in Neo4j

            //userNeo4jDAO.addUser(userDTO);
            neo4j = true;
            session.commitTransaction();

            return userDTO;
        } catch (Exception e) {
            session.abortTransaction();
            return null; // Return null or handle the error appropriately
        } finally {
            session.close();
        }
    }

    public UserDTO getUserByUsername(String username) {
        try {

            System.out.println("Retrieving user by username: " + username);
            UserDTO found = userMongoDAO.getUserByUsername(username);
            System.out.println("User found: " + found);
            return found;
        } catch (Exception e) {
            throw new RuntimeException("User not found: " + username, e);
        }
    }

    public UserDTO editUser(UserDTO userDTO) {
        return UserDTO.fromUser(userMongoDAO.editUser(userDTO));
    }


}
