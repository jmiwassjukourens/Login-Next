package com.app.springbootcrud.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.app.springbootcrud.controllers.dto.UserUpdateDTO;
import com.app.springbootcrud.entities.User;
import com.app.springbootcrud.services.UserService;



import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private ErrorHandler errorHandler;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping
    public List<User> list() {
        return service.findAll();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            User user = service.findById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return errorHandler.createNotFoundResponse("User");
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasFieldErrors()) {
            return errorHandler.createValidationErrorResponse(extractValidationErrors(result));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return errorHandler.createNotFoundResponse("User");
        }
    }


@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
@PutMapping("/{id}")
public ResponseEntity<?> update(
        @PathVariable Long id,
        @Valid @RequestBody UserUpdateDTO userUpdateDTO,
        BindingResult result
) {
    if (result.hasFieldErrors()) {
        return errorHandler.createValidationErrorResponse(extractValidationErrors(result));
    }
    
    try {
        // Create a User object with only the username for update
        User userDetails = new User();
        userDetails.setUsername(userUpdateDTO.getUsername());
        
        User updatedUser = service.update(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    } catch (RuntimeException e) {
        return errorHandler.createNotFoundResponse("User");
    }
}


 
    private Map<String, String> extractValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "The field " + err.getField() + " " + err.getDefaultMessage());
        });
        return errors;
    }






}
