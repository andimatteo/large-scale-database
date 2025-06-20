package it.unipi.githeritage.Service;

import it.unipi.githeritage.DTO.PathDTO;
import it.unipi.githeritage.DTO.UserMetadataDTO;
import it.unipi.githeritage.Repository.MongoDB.MongoUserRepository;
import it.unipi.githeritage.Repository.Neo4j.NeoUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import it.unipi.githeritage.DAO.MongoDB.UserMongoDAO;
import it.unipi.githeritage.DAO.Neo4j.Neo4jDAO;
import it.unipi.githeritage.DTO.UserDTO;
import lombok.AllArgsConstructor;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private final MongoClient mongoClient;

    @Autowired
    private UserMongoDAO userMongoDAO;

    @Autowired
    private Neo4jDAO neo4jDAO;

    @Autowired
    private MongoUserRepository mongoUserRepository;

    @Autowired
    private NeoUserRepository neoUserRepository;

    public UserDTO addUser(UserDTO userDTO) {
        ClientSession session = mongoClient.startSession();
        // todo vedere cosa fare con questa variabile neo4j
        Boolean neo = false;
        try {
            System.out.println("Starting transaction to add user: " + userDTO.getUsername());
            session.startTransaction();
            // Save the user in MongoDB
            userDTO.setIsAdmin(false); // Default to non-admin
            //userDTO.setRegistrationDate(LocalDate.now()); // Set registration date to current date
            if (mongoUserRepository.existsById(userDTO.getUsername())) {
                throw new RuntimeException("Username already exists");
            }

            UserDTO addedUser = UserDTO.fromUser(userMongoDAO.addUser(userDTO));


            // Save the user in Neo4j
            neo4jDAO.mergeUser(userDTO.getUsername());
            neo = true;
            session.commitTransaction();

            return addedUser;
        } catch (Exception e) {
            session.abortTransaction();
            if (neo) {
                throw new RuntimeException("Error adding data to moongoDB but Neo4j data added," +
                        "check for consistency: " + userDTO.getUsername());
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public Long deleteUser(String username) {
        return mongoUserRepository.deleteByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
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

    public UserDTO getUser(String username) {
        return mongoUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public List<String> getFollowersUsernames(String username) {
        return neoUserRepository.findFollowersUsernamesByUsername(username);
    }

    public List<String> getFollowsUsernames(String username) {
        return neoUserRepository.findFollowsUsernamesByUsername(username);
    }

    public boolean followUser(String followerUsername, String followedUsername) {
        ClientSession session = mongoClient.startSession();
        try {
            System.out.println("Starting transaction to follow user: " + followerUsername + " -> " + followedUsername);
            session.startTransaction();
            
            // Create the FOLLOWS relationship in Neo4j
            neo4jDAO.followUser(followerUsername, followedUsername);
            
            session.commitTransaction();
            return true;
        } catch (Exception e) {
            session.abortTransaction();
            System.err.println("Error following user: " + e.getMessage());
            return false;
        } finally {
            session.close();
        }
    }

    public boolean unfollowUser(String followerUsername, String followedUsername) {
        ClientSession session = mongoClient.startSession();
        try {
            System.out.println("Starting transaction to unfollow user: " + followerUsername + " -> " + followedUsername);
            session.startTransaction();
            
            // Remove the FOLLOWS relationship in Neo4j
            neo4jDAO.unfollowUser(followerUsername, followedUsername);
            
            session.commitTransaction();
            return true;
        } catch (Exception e) {
            session.abortTransaction();
            System.err.println("Error unfollowing user: " + e.getMessage());
            return false;
        } finally {
            session.close();
        }
    }

    public PathDTO getFollowPath(String from, String to) {
        return neo4jDAO.getFollowPath(from, to)
                .orElse(new PathDTO(-1, List.of()));
    }

    public PathDTO getProjectPath(String from, String to) {
        return neo4jDAO.getProjectPath(from, to)
                .orElse(new PathDTO(-1, List.of()));
    }

    public List<it.unipi.githeritage.Model.Neo4j.User> discoverPeople(String username) {
        return neoUserRepository.findSuggestedPeople(username);
    }

    // get 200 users
    public List<UserMetadataDTO> getAllUserMetadata() {
        return mongoUserRepository.findTop100ByOrderByUsernameAsc()
                .orElse(null);
    }

    // get 200 users
    public List<UserMetadataDTO> getAllUsersMetadataPaginated(int pageIndex) {
        return mongoUserRepository
                .findAllOrderByUsername(PageRequest.of(pageIndex, 50))
                .getContent();  // prendi solo la lista interna
    }


}
