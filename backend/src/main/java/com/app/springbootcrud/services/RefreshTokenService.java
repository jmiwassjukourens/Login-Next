package com.app.springbootcrud.services;

import com.app.springbootcrud.configuration.RefreshToken;

public interface RefreshTokenService {

    RefreshToken create(String username);

    RefreshToken validate(String token);

    void invalidateByUsername(String username);
}