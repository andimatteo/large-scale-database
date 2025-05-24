package it.unipi.githeritage.service.mongodb;

import it.unipi.githeritage.model.mongodb.File;
import it.unipi.githeritage.model.mongodb.User;
import it.unipi.githeritage.repository.mongodb.MongoFileRepository;
import it.unipi.githeritage.repository.mongodb.MongoUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoFileService {

    @Autowired
    MongoFileRepository mongoFileRepository;

    public boolean addFile(File file){
        return mongoFileRepository.save(file)!=null;
    }

    public void dropFileCollection() {
        mongoFileRepository.deleteAll();
    }
}