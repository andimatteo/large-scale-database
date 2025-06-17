package it.unipi.githeritage.DAO.MongoDB;

import com.mongodb.client.MongoClient;
import it.unipi.githeritage.Model.MongoDB.File;
import it.unipi.githeritage.Repository.MongoDB.MongoFileRepository;
import it.unipi.githeritage.Repository.MongoDB.MongoProjectRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
public class FileMongoDAO {

    @Autowired
    private MongoFileRepository mongoFileRepository;
    @Autowired
    private MongoProjectRepository mongoProjectRepository;
    @Autowired
    private MongoClient mongo;
}
