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
import com.app.springbootcrud.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;


import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import static com.app.springbootcrud.security.TokenJwtConfig.*;
public class JwtAuthenticationFilter
        extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

    private static final long EXPIRATION =
        20 * 60 * 1000; 

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
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

            return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    user.getPassword()
                )
            );
        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid login request");
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

        String token = Jwts.builder()
            .subject(user.getUsername())
            .claim(
                "roles",
                authResult.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList()
            )
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(SECRET_KEY)
            .compact();

        ResponseCookie jwtCookie = ResponseCookie
            .from(jwtProperties.getCookie().getName(), token)
            .httpOnly(jwtProperties.getCookie().isHttpOnly())
            .secure(jwtProperties.getCookie().isSecure())
            .sameSite(jwtProperties.getCookie().getSameSite())
            .path(jwtProperties.getCookie().getPath())
            .maxAge(jwtProperties.getExpirationMs() / 1000)
            .build();


        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.setContentType("application/json");

        response.getWriter().write("""
            { "message": "Login successful" }
        """);
    }
}
