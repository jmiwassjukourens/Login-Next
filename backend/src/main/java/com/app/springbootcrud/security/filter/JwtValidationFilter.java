package com.app.springbootcrud.security.filter;


import static com.app.springbootcrud.security.TokenJwtConfig.SECRET_KEY;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.springbootcrud.configuration.JwtProperties;

import jakarta.servlet.http.Cookie;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;

    public JwtValidationFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        String token = null;
        String cookieName =
            jwtProperties.getAccessCookie().getName();

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String username = claims.getSubject();
            @SuppressWarnings("unchecked")
            List<String> roles =
                (List<String>) claims.get("roles", List.class);

            var authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

            var auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
            );

            SecurityContextHolder.getContext()
                .setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter()
                .write("{\"message\":\"Invalid or expired token\"}");
        }
    }
}
