package com.app.springbootcrud.boostrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.app.springbootcrud.entities.Role;
import com.app.springbootcrud.entities.User;
import com.app.springbootcrud.repositories.RoleRepository;
import com.app.springbootcrud.repositories.UserRepository;

@Component
public class AppInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_USER");
        createUserIfNotExists("juan", "test123");
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.findByName(roleName).isPresent()) {
            Role role = new Role(roleName);
            roleRepository.save(role);
            System.out.println("Role created: " + roleName);
        }
    }

    private void createUserIfNotExists(String username, String password) {
        if (!userRepository.findByUsername(username).isPresent()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            
            roleRepository.findByName("ROLE_ADMIN").ifPresent(role -> user.getRoles().add(role));
            roleRepository.findByName("ROLE_USER").ifPresent(role -> user.getRoles().add(role));
            
            userRepository.save(user);
            System.out.println("User created: " + username);
        }
    }
}
