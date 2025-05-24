package it.unipi.githeritage.service.neo4j;


import it.unipi.githeritage.model.neo4j.User;
import it.unipi.githeritage.repository.neo4j.Neo4jUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Neo4jUserService {

    @Autowired
    Neo4jUserRepository userRepository;

    // create new user
    public User createUser(String username) {
        User u = new User(username);
        return userRepository.save(u);
    }
}

