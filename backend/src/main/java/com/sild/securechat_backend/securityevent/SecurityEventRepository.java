package com.sild.securechat_backend.securityevent;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    // Custom query methods can be added here if needed

    List<SecurityEvent> findTop50ByOrderByCreatedAtDesc();

    List<SecurityEvent> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByEventTypeAndCreatedAtAfter(SecurityEventType eventType, LocalDateTime createdAt);

    long countBySeverityAndCreatedAtAfter(SecuritySeverity severity, LocalDateTime createdAt);
}
