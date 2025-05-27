package it.unipi.githeritage.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import it.unipi.githeritage.service.mongodb.MongoUserService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MongoUserService userService;

    @GetMapping("/all")
    public List<it.unipi.githeritage.model.mongodb.User> getAll(){
        return userService.getAll();
    }


}
