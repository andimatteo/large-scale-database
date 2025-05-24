package it.unipi.githeritage.service.mongodb;

import it.unipi.githeritage.model.mongodb.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import it.unipi.githeritage.repository.mongodb.MongoUserRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class MongoUserService {

    @Autowired
    MongoUserRepository userRepository;

    public List<User> getAll(){
        return userRepository.findAll();
    }

    public boolean addUser(User user){
        return userRepository.save(user)!=null;
    }

    public void dropUserDatabase(){
        userRepository.deleteAll();
    }
}
