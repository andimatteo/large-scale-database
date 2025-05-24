package it.unipi.githeritage.service;

import it.unipi.githeritage.repository.mongodb.MongoUserRepository;
import it.unipi.githeritage.repository.neo4j.Neo4jUserRepository;
import it.unipi.githeritage.utils.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private final MongoUserRepository mongoUserRepo;

    @Autowired
    private final Neo4jUserRepository neo4jUserRepo;

    public AuthService(MongoUserRepository userRepo, Neo4jUserRepository neo4jUserRepo) {
        this.mongoUserRepo = userRepo;
        this.neo4jUserRepo = neo4jUserRepo;
    }

    public boolean register(String username, String password) {
        if (mongoUserRepo.findByUsername(username).isPresent()) {
            return false; // username già in uso
        }
        mongoUserRepo.save(new it.unipi.githeritage.model.mongodb.User(Role.USER,username, password,null));
        neo4jUserRepo.save(new it.unipi.githeritage.model.neo4j.User(username));
        return true;
    }

    public boolean login(String username, String password) {
        return mongoUserRepo.findByUsername(username)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
}