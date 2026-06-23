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

@Service
public class AuthService {
    private final UserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
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

        return new AuthResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole().name(),
            "User registered successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = appUserRepository.findByUsername(request.usernameOrEmail())
            .or(() -> appUserRepository.findByEmail(request.usernameOrEmail()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password"));
        
        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
        }

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is disabled");
        }

        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            "Login successful"
        );
    }
}
