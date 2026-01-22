package com.app.springbootcrud.security.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        CsrfToken csrfToken =
            (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            // Set CSRF token as cookie accessible to JavaScript
            ResponseCookie cookie = ResponseCookie
                .from("XSRF-TOKEN", csrfToken.getToken())
                .httpOnly(false) // Must be accessible to JavaScript for CSRF protection
                .secure(request.isSecure()) // Use secure in HTTPS
                .sameSite("Lax")
                .path("/")
                .build();
            
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        filterChain.doFilter(request, response);
    }
}
