package com.sild.securechat_backend.admin;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sild.securechat_backend.auth.dto.AuthResponse;
import com.sild.securechat_backend.securityevent.SecurityEventService;
import com.sild.securechat_backend.securityevent.dto.SecurityEventResponse;
import com.sild.securechat_backend.securityevent.dto.SecurityStatsResponse;
import com.sild.securechat_backend.user.User;


@RestController
@RequestMapping("/api/admin/security")
public class AdminSecurityController {

    private final SecurityEventService securityEventService;

    public AdminSecurityController(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @GetMapping("/test")
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

    @GetMapping("/events/recent")
    public List<SecurityEventResponse> recentEvents() {
        return securityEventService.getRecentEvents();
    }

    @GetMapping("/events/user/{userId}")
    public List<SecurityEventResponse> eventsForUser(@PathVariable Long userId) {
        return securityEventService.getEventsForUser(userId);
    }

    @GetMapping("/stats")
    public SecurityStatsResponse stats() {
        return securityEventService.getStatsForLast24Hours();
    }
}
