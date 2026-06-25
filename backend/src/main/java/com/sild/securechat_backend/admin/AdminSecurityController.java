package com.sild.securechat_backend.admin;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sild.securechat_backend.auth.dto.AuthResponse;
import com.sild.securechat_backend.user.User;


@RestController
public class AdminSecurityController {
    
    @GetMapping("/api/admin/security/test")
    public AuthResponse adminTest(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            "Admin endpoint access granted"
        );
    }
    
}
