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
import com.app.springbootcrud.services.RefreshTokenService;

import io.jsonwebtoken.Jwts;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;

    public AuthController(
            RefreshTokenService refreshTokenService,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenService = refreshTokenService;
        this.jwtProperties = jwtProperties;
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

        String newAccessToken = Jwts.builder()
            .subject(token.getUsername())
            .issuedAt(new java.util.Date())
            .expiration(new java.util.Date(
                System.currentTimeMillis()
                    + jwtProperties.getAccessExpirationMs()
            ))
            .signWith(SECRET_KEY)
            .compact();

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

        return ResponseEntity.ok(Map.of("message", "Token refreshed"));
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
    public ResponseEntity<?> logout(HttpServletResponse response) {

        JwtProperties.Cookie access = jwtProperties.getAccessCookie();

        ResponseCookie deleteCookie = ResponseCookie
            .from(access.getName(), "")
            .httpOnly(access.isHttpOnly())
            .secure(access.isSecure())
            .sameSite(access.getSameSite())
            .path(access.getPath())
            .maxAge(0)
            .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            deleteCookie.toString()
        );

        return ResponseEntity.ok(
            Map.of("message", "Logout successful")
        );
    }

}

