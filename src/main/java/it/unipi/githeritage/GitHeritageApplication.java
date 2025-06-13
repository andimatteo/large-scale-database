package it.unipi.githeritage;

import it.unipi.githeritage.model.mongodb.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import it.unipi.githeritage.service.mongodb.UserService;
import it.unipi.githeritage.utils.Role;

@SpringBootApplication
public class GitHeritageApplication {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(GitHeritageApplication.class, args);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void init(){
        // check presence of admin user
        //userService.getAll().forEach(System.out::println);
    }

}
