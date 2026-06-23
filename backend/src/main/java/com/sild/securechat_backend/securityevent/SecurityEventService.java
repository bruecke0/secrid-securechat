package com.sild.securechat_backend.securityevent;

import org.springframework.stereotype.Service;

@Service
public class SecurityEventService {
    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    public void logEvent(
        Long userId,
        SecurityEventType eventType,
        SecuritySeverity severity,
        String ipAddress,
        String details
    ) {
        SecurityEvent event = new SecurityEvent(
            userId,
            eventType,
            severity,
            ipAddress,
            details
        );
        securityEventRepository.save(event);
    }
}
