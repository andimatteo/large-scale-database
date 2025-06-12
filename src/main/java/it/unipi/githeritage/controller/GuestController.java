package it.unipi.githeritage.controller;

import org.apache.catalina.filters.AddDefaultCharsetFilter.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unipi.githeritage.DTO.ResponseDTO;
import it.unipi.githeritage.DTO.UserDTO;
import it.unipi.githeritage.service.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/guest")
@AllArgsConstructor
public class GuestController {

    private final UserService userService;
    
    // Add new User
    @PostMapping("/user")
    public ResponseEntity<ResponseDTO<UserDTO>> addUser(@RequestBody UserDTO userDTO) {
        // Here you would typically call a service to save the user to the database.
        // For now, we will just return the userDTO as a response.

        UserDTO addedUser = userService.addUser(userDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "User created successfully", addedUser));
    }

}
