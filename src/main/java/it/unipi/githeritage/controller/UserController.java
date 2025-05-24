package it.unipi.githeritage.controller;


import it.unipi.githeritage.repository.neo4j.Neo4jUserRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import it.unipi.githeritage.service.mongodb.MongoUserService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MongoUserService userService;

    @Autowired
    private Neo4jUserRepository neo4jUserRepository;

    @GetMapping("/all")
    public List<it.unipi.githeritage.model.mongodb.User> getAll(){
        return userService.getAll();
    }


}
