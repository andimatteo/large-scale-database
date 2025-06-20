package it.unipi.githeritage.Populate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unipi.githeritage.DAO.MongoDB.UserMongoDAO;
import it.unipi.githeritage.DAO.Neo4j.Neo4jDAO;
import it.unipi.githeritage.Model.MongoDB.User;
import lombok.RequiredArgsConstructor;

@Profile("populateuser")
@Component
@RequiredArgsConstructor
public class UsersMongo2Neo4j implements CommandLineRunner {

    private final Neo4jDAO neo4jDAO;
    private final UserMongoDAO userMongoDAO;
    private static final int BATCH_SIZE = 100; // Process users in batches of 500

    @Override
    public void run(String... args) throws Exception {
        // Create index on username for better performance
        neo4jDAO.createUsernameIndex();
        System.out.println("Created constraint on username attribute in Neo4j");
        
        // Read JSON file and parse it
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File jsonFile = new File("scraped_data/mongo_users.json");
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            
            int totalUsers = rootNode.size();
            System.out.println("Total users to process: " + totalUsers);
            
            // Extract all usernames into a list
            List<String> allUsernames = new ArrayList<>(totalUsers);
            for (JsonNode userNode : rootNode) {
                allUsernames.add(userNode.get("_id").asText());
            }
            
            // Process usernames in batches
            for (int i = 0; i < allUsernames.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, allUsernames.size());
                List<String> batch = allUsernames.subList(i, endIndex);
                
                // Merge all users in this batch
                neo4jDAO.mergeUsers(batch);
                
                System.out.println("[User Progress: Batch " + (i/BATCH_SIZE + 1) + 
                                  "] Processed users " + (i+1) + "-" + endIndex + 
                                  " of " + totalUsers);
            }

            
            
            // Process follower relationships in batches
            int progress = 0;
            for (int i = 0;i < totalUsers; i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, totalUsers);
                List<Map<String, String>> followRelationships = new ArrayList<>();
                
                for (int j = i; j < endIndex; j++) {
                    JsonNode userNode = rootNode.get(j);
                    progress++;
                    
                    int followerNumber = userNode.get("followerNumber").asInt();
                    String userId = userNode.get("_id").asText();
                    Set<String> chosenFollowers = new HashSet<>();
                    Random random = new Random();

                    while (chosenFollowers.size() < followerNumber && chosenFollowers.size() < totalUsers - 1) {
                        int idx = random.nextInt(totalUsers);
                        JsonNode potentialFollower = rootNode.get(idx);
                        String followerId = potentialFollower.get("_id").asText();

                        if (!followerId.equals(userId) && !chosenFollowers.contains(followerId)) {
                            chosenFollowers.add(followerId);
                            followRelationships.add(Map.of("follower", followerId, "followed", userId));
                        }
                    }
                    
                    System.out.println("[Follow Progress: " + progress + "/" + totalUsers + 
                                      " (" + chosenFollowers.size() + "/" + followerNumber + ")] " + 
                                      "Added " + chosenFollowers.size() + " followers for user: " + userId);
                }
                
                // Process all follow relationships in this batch
                if (!followRelationships.isEmpty()) {
                    neo4jDAO.followUsers(followRelationships);
                }
            }

            // Update MongoDB with follower counts from Neo4j
            progress = 0;
            for (JsonNode userNode : rootNode) {
                progress++;
                String userId = userNode.get("_id").asText();
                
                System.out.println("[Redundancy Progress: " + progress + "/" + totalUsers + 
                                  "] Processing user: " + userId);

                // Count incoming and outgoing edges in Neo4j
                int followerCount = neo4jDAO.countFollower(userId);   // incoming edges: followers
                int followingCount = neo4jDAO.countFollowing(userId);  // outgoing edges: following

                // Update MongoDB with the new counts
                User emptyUser = new User();
                emptyUser.setUsername(userId);
                emptyUser.setFollowerNumber(followerCount);
                emptyUser.setFollowingNumber(followingCount);

                userMongoDAO.editFollowRedundancy(emptyUser);
            }

            System.out.println("User population completed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
