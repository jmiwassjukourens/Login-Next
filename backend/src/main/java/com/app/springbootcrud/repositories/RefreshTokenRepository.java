package com.app.springbootcrud.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.springbootcrud.configuration.RefreshToken;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsername(String username);

    void deleteByExpiresAtBefore(Instant now);
}
