package com.app.springbootcrud.security.filter;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.app.springbootcrud.configuration.JwtProperties;
import com.app.springbootcrud.configuration.RefreshToken;
import com.app.springbootcrud.entities.User;
import com.app.springbootcrud.services.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;


import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import static com.app.springbootcrud.security.TokenJwtConfig.*;
public class JwtAuthenticationFilter
        extends UsernamePasswordAuthenticationFilter {

    private static final String USERNAME_ATTRIBUTE = "LOGIN_USERNAME";

    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final AccountLockoutFilter accountLockoutFilter;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtProperties jwtProperties,
            RefreshTokenService refreshTokenService,
            AccountLockoutFilter accountLockoutFilter
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
        this.accountLockoutFilter = accountLockoutFilter;
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {

        try {
            User user = new ObjectMapper()
                .readValue(request.getInputStream(), User.class);

            // Store username in request attribute for use in failure handler
            request.setAttribute(USERNAME_ATTRIBUTE, user.getUsername());

            // Check if account is locked before attempting authentication
            accountLockoutFilter.checkAccountLocked(user.getUsername());

            return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    user.getPassword()
                )
            );

        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid login request", e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException {

        var user =
            (org.springframework.security.core.userdetails.User)
                authResult.getPrincipal();


        String accessToken = Jwts.builder()
            .subject(user.getUsername())
            .claim(
                "roles",
                authResult.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList()
            )
            .issuedAt(new Date())
            .expiration(
                new Date(System.currentTimeMillis()
                    + jwtProperties.getAccessExpirationMs())
            )
            .signWith(SECRET_KEY)
            .compact();

   
        JwtProperties.Cookie accessCookieProps =
            jwtProperties.getAccessCookie();

        ResponseCookie accessCookie = ResponseCookie
            .from(accessCookieProps.getName(), accessToken)
            .httpOnly(accessCookieProps.isHttpOnly())
            .secure(accessCookieProps.isSecure())
            .sameSite(accessCookieProps.getSameSite())
            .path(accessCookieProps.getPath())
            .maxAge(jwtProperties.getAccessExpirationMs() / 1000)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        
        // Create and set refresh token
        RefreshToken refreshToken = refreshTokenService.create(user.getUsername());
        
        JwtProperties.Cookie refreshCookieProps =
            jwtProperties.getRefreshCookie();
        
        ResponseCookie refreshCookie = ResponseCookie
            .from(refreshCookieProps.getName(), refreshToken.getToken())
            .httpOnly(refreshCookieProps.isHttpOnly())
            .secure(refreshCookieProps.isSecure())
            .sameSite(refreshCookieProps.getSameSite())
            .path(refreshCookieProps.getPath())
            .maxAge(jwtProperties.getRefreshExpirationMs() / 1000)
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.setContentType("application/json");

        // Reset failed login attempts on successful login
        accountLockoutFilter.handleSuccessfulLogin(user.getUsername());

        response.getWriter().write("""
            { "message": "Login successful" }
        """);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {
        // Get username from request attribute set during authentication attempt
        String username = (String) request.getAttribute(USERNAME_ATTRIBUTE);
        if (username != null) {
            accountLockoutFilter.handleFailedLogin(username);
        }

        super.unsuccessfulAuthentication(request, response, failed);
    }
}
