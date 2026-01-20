package com.app.springbootcrud.services.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.springbootcrud.configuration.JwtProperties;
import com.app.springbootcrud.configuration.RefreshToken;
import com.app.springbootcrud.repositories.RefreshTokenRepository;
import com.app.springbootcrud.services.RefreshTokenService;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository repository,
            JwtProperties jwtProperties
    ) {
        this.repository = repository;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public RefreshToken create(String username) {
        String token = UUID.randomUUID().toString();

        Instant expiresAt = Instant.now()
                .plusMillis(jwtProperties.getRefreshExpirationMs());

        RefreshToken refreshToken =
                new RefreshToken(token, username, expiresAt);

        return repository.save(refreshToken);
    }

    @Override
    public RefreshToken validate(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void invalidateByUsername(String username) {
        repository.deleteByUsername(username);
    }
}
