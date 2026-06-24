package com.sild.securechat_backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sild.securechat_backend.auth.dto.AuthResponse;
import com.sild.securechat_backend.auth.dto.LoginRequest;
import com.sild.securechat_backend.auth.dto.RegisterRequest;
import com.sild.securechat_backend.user.Role;
import com.sild.securechat_backend.user.User;
import com.sild.securechat_backend.user.UserRepository;
import com.sild.securechat_backend.securityevent.SecurityEventService;
import com.sild.securechat_backend.securityevent.SecurityEventType;
import com.sild.securechat_backend.securityevent.SecuritySeverity;


import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class AuthService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15; 

    private final UserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityEventService securityEventService;

    public AuthService(UserRepository appUserRepository, PasswordEncoder passwordEncoder, SecurityEventService securityEventService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityEventService = securityEventService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (appUserRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User newUser = new User(
            request.username(),
            request.email(),
            hashedPassword,
            Role.USER
        );

        User savedUser = appUserRepository.save(newUser);

        securityEventService.logEvent(
            savedUser.getId(),
            SecurityEventType.REGISTER_SUCCESS,
            SecuritySeverity.LOW,
            null, // IP address can be logged if available
            "User registered successfully"
        );

        return new AuthResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole().name(),
            "User registered successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOptional = appUserRepository.findByUsername(request.usernameOrEmail())
            .or(() -> appUserRepository.findByEmail(request.usernameOrEmail()));
        
        if (userOptional.isEmpty()) {
            securityEventService.logEvent(
                null,
                SecurityEventType.LOGIN_FAILURE,
                SecuritySeverity.MEDIUM,
                null, // IP address can be logged if available
                "Login failed for username/email: " + request.usernameOrEmail()
            );
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
        }

        User user = userOptional.get();

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is disabled");
        }

        if (isAccountLocked(user)) {
            securityEventService.logEvent(
                user.getId(),
                SecurityEventType.ACCOUNT_LOCKED,
                SecuritySeverity.HIGH,
                null, // IP address can be logged if available
                "Account locked due to multiple failed login attempts"
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is locked due to multiple failed login attempts. Please try again later.");
        }

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        
        if (!passwordMatches) {
            handleFailedLogin(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
        }

        resetFailedLoginAttempts(user);
        
        securityEventService.logEvent(
            user.getId(),
            SecurityEventType.LOGIN_SUCCESS,
            SecuritySeverity.LOW,
            null, // IP address can be logged if available
            "User logged in successfully"
        );
        
        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            "Login successful"
        );
    }

    private boolean isAccountLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private void handleFailedLogin(User user) {
        int failedAttempts = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(failedAttempts);

        if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
            appUserRepository.save(user);

            securityEventService.logEvent(
                user.getId(),
                SecurityEventType.ACCOUNT_LOCKED,
                SecuritySeverity.HIGH,
                null, // IP address can be logged if available
                "Account locked due to multiple failed login attempts"
            );

            throw new ResponseStatusException(HttpStatus.LOCKED, "Account is locked due to multiple failed login attempts. Please try again later.");
        }
        appUserRepository.save(user);

        securityEventService.logEvent(
            user.getId(),
            SecurityEventType.LOGIN_FAILURE,
            SecuritySeverity.MEDIUM,
            null, // IP address can be logged if available
            "Login failed for username/email: " + user.getUsername()
        );
    }

    private void resetFailedLoginAttempts(User user) {
        if (user.getFailedLoginCount() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            appUserRepository.save(user);
        }
    }
}
