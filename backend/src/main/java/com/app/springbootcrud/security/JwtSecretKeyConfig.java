package com.app.springbootcrud.security;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

@Configuration
public class JwtSecretKeyConfig {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void initializeSecretKey() {
        String secretKeyString = System.getenv("JWT_SECRET_KEY");
        
        if (secretKeyString == null || secretKeyString.isEmpty()) {
            // Check if production profile is active
            boolean isProduction = environment.getActiveProfiles().length > 0 &&
                java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
            
            if (isProduction) {
                throw new IllegalStateException(
                    "JWT_SECRET_KEY environment variable must be set in production"
                );
            }
            
            // Generate for development only
            SecretKey generatedKey = Jwts.SIG.HS256.key().build();
            secretKeyString = Base64.getEncoder().encodeToString(generatedKey.getEncoded());
            System.out.println("WARNING: Generated JWT secret key for development. " +
                             "Set JWT_SECRET_KEY environment variable for production.");
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
            TokenJwtConfig.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY must be a valid Base64-encoded 256-bit key", e
            );
        }
    }
}
