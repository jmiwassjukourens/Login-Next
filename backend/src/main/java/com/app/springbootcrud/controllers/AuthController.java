package com.app.springbootcrud.controllers;

import static com.app.springbootcrud.security.TokenJwtConfig.SECRET_KEY;


import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.springbootcrud.configuration.JwtProperties;
import com.app.springbootcrud.configuration.RefreshToken;
import com.app.springbootcrud.entities.User;
import com.app.springbootcrud.services.RefreshTokenService;
import com.app.springbootcrud.services.UserService;

import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

import java.util.HashMap;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    @Autowired
    private ErrorHandler errorHandler;

    public AuthController(
            RefreshTokenService refreshTokenService,
            JwtProperties jwtProperties,
            UserService userService
    ) {
        this.refreshTokenService = refreshTokenService;
        this.jwtProperties = jwtProperties;
        this.userService = userService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (jwtProperties.getRefreshCookie()
                        .getName()
                        .equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        RefreshToken token =
            refreshTokenService.validate(refreshToken);

        String username = token.getUsername();

        // Rotate refresh token: invalidate old one and create new one
        refreshTokenService.invalidateByUsername(username);
        RefreshToken newRefreshToken = refreshTokenService.create(username);

        // Create new access token
        String newAccessToken = Jwts.builder()
            .subject(username)
            .issuedAt(new java.util.Date())
            .expiration(new java.util.Date(
                System.currentTimeMillis()
                    + jwtProperties.getAccessExpirationMs()
            ))
            .signWith(SECRET_KEY)
            .compact();

        // Set new access token cookie
        JwtProperties.Cookie accessProps =
            jwtProperties.getAccessCookie();

        ResponseCookie accessCookie = ResponseCookie
            .from(accessProps.getName(), newAccessToken)
            .httpOnly(accessProps.isHttpOnly())
            .secure(accessProps.isSecure())
            .sameSite(accessProps.getSameSite())
            .path(accessProps.getPath())
            .maxAge(jwtProperties.getAccessExpirationMs() / 1000)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        // Set new refresh token cookie
        JwtProperties.Cookie refreshProps =
            jwtProperties.getRefreshCookie();

        ResponseCookie refreshCookie = ResponseCookie
            .from(refreshProps.getName(), newRefreshToken.getToken())
            .httpOnly(refreshProps.isHttpOnly())
            .secure(refreshProps.isSecure())
            .sameSite(refreshProps.getSameSite())
            .path(refreshProps.getPath())
            .maxAge(jwtProperties.getRefreshExpirationMs() / 1000)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Token refreshed"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody User user,
            BindingResult result
    ) {
        if (result.hasFieldErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err -> {
                errors.put(err.getField(), "The field " + err.getField() + " " + err.getDefaultMessage());
            });
            return errorHandler.createValidationErrorResponse(errors);
        }

        // Ensure new users are not admins by default
        user.setAdmin(false);
        
        try {
            User savedUser = userService.save(user);
            // Don't return password
            savedUser.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return errorHandler.createErrorResponse(
                "Registration failed",
                "Registration failed: " + e.getMessage(),
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return ResponseEntity.ok(
            Map.of("username", authentication.getName())
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Delete access token cookie
        JwtProperties.Cookie access = jwtProperties.getAccessCookie();
        ResponseCookie deleteAccessCookie = ResponseCookie
            .from(access.getName(), "")
            .httpOnly(access.isHttpOnly())
            .secure(access.isSecure())
            .sameSite(access.getSameSite())
            .path(access.getPath())
            .maxAge(0)
            .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            deleteAccessCookie.toString()
        );

        // Delete refresh token cookie
        JwtProperties.Cookie refresh = jwtProperties.getRefreshCookie();
        ResponseCookie deleteRefreshCookie = ResponseCookie
            .from(refresh.getName(), "")
            .httpOnly(refresh.isHttpOnly())
            .secure(refresh.isSecure())
            .sameSite(refresh.getSameSite())
            .path(refresh.getPath())
            .maxAge(0)
            .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            deleteRefreshCookie.toString()
        );

        // Invalidate refresh token in database if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                refreshTokenService.invalidateByUsername(authentication.getName());
            } catch (Exception e) {
                // Log error but don't fail logout
                System.err.println("Error invalidating refresh token: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(
            Map.of("message", "Logout successful")
        );
    }

}

