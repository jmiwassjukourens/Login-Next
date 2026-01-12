package com.app.springbootcrud.security.filter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.app.springbootcrud.entities.User;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import static com.app.springbootcrud.security.TokenJwtConfig.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        User user = null;
        String username = null;
        String password = null;

        try {
            user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            username = user.getUsername();
            password = user.getPassword();
        } catch (StreamReadException e) {
            e.printStackTrace();
        } catch (DatabindException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        return authenticationManager.authenticate(authenticationToken);
    }

@Override
protected void successfulAuthentication(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain,
        Authentication authResult
) throws IOException {

    var user = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
    String username = user.getUsername();

    Claims claims = Jwts.claims()
        .add("authorities", new ObjectMapper().writeValueAsString(authResult.getAuthorities()))
        .build();

    String token = Jwts.builder()
        .subject(username)
        .claims(claims)
        .expiration(new Date(System.currentTimeMillis() + 3600000))
        .issuedAt(new Date())
        .signWith(SECRET_KEY)
        .compact();

ResponseCookie jwtCookie = ResponseCookie.from("JWT_TOKEN", token)
    .httpOnly(true)
    .secure(true)
    .sameSite("None") 
    .path("/")
    .maxAge(3600)
    .build();

response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());


    Map<String, String> body = new HashMap<>();
    body.put("message", "Login successful");
    body.put("username", username);

    response.setContentType(CONTENT_TYPE);
    response.getWriter().write(new ObjectMapper().writeValueAsString(body));
}


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Incorrect username or password authentication error!");
        body.put("error", failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(401);
        response.setContentType(CONTENT_TYPE);
    }

}
