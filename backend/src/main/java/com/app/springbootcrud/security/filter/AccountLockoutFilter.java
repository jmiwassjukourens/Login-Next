package com.app.springbootcrud.security.filter;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.springbootcrud.repositories.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AccountLockoutFilter extends OncePerRequestFilter {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // This filter runs before authentication
        // We'll check lockout status in the authentication failure handler
        filterChain.doFilter(request, response);
    }

    public void handleFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLockedUntil(
                    LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES)
                );
            }

            userRepository.save(user);
        });
    }

    public void handleSuccessfulLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        });
    }

    public void checkAccountLocked(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.isAccountLocked()) {
                throw new BadCredentialsException(
                    "Account is locked due to too many failed login attempts. " +
                    "Please try again later."
                );
            }
        });
    }
}
