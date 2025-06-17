package it.unipi.githeritage.Service;

import it.unipi.githeritage.Repository.MongoDB.MongoCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommitService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoCommitRepository mongoCommitRepository;

}
