package com.sild.securechat_backend.securityevent;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sild.securechat_backend.securityevent.dto.SecurityEventResponse;
import com.sild.securechat_backend.securityevent.dto.SecurityStatsResponse;

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

    public List<SecurityEventResponse> getRecentEvents() {
        return securityEventRepository.findTop50ByOrderByCreatedAtDesc()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    public List<SecurityEventResponse> getEventsForUser(long userId) {
        return securityEventRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    public SecurityStatsResponse getStatsForLast24Hours() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        long loginSuccessCount = securityEventRepository.countByEventTypeAndCreatedAtAfter(
                SecurityEventType.LOGIN_SUCCESS,
                since
        );

        long loginFailedCount = securityEventRepository.countByEventTypeAndCreatedAtAfter(
                SecurityEventType.LOGIN_FAILED,
                since
        );

        long accountLockedCount = securityEventRepository.countByEventTypeAndCreatedAtAfter(
                SecurityEventType.ACCOUNT_LOCKED,
                since
        );

        long registerSuccessCount = securityEventRepository.countByEventTypeAndCreatedAtAfter(
                SecurityEventType.REGISTER_SUCCESS,
                since
        );

        long highSeverityCount = securityEventRepository.countBySeverityAndCreatedAtAfter(
                SecuritySeverity.HIGH,
                since
        );

        long criticalSeverityCount = securityEventRepository.countBySeverityAndCreatedAtAfter(
                SecuritySeverity.CRITICAL,
                since
        );

        return new SecurityStatsResponse(
                loginSuccessCount,
                loginFailedCount,
                accountLockedCount,
                registerSuccessCount,
                highSeverityCount,
                criticalSeverityCount
        );
    }

    private SecurityEventResponse mapToResponse(SecurityEvent event) {
        return new SecurityEventResponse(
                event.getId(),
                event.getUserId(),
                event.getEventType().name(),
                event.getSeverity().name(),
                event.getIpAddress(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}
