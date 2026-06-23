package com.sild.securechat_backend.securityevent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SecurityEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SecuritySeverity severity;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SecurityEvent() {
    }

    public SecurityEvent(Long userId, SecurityEventType eventType, SecuritySeverity severity, String ipAddress, String details) {
        this.userId = userId;
        this.eventType = eventType;
        this.severity = severity;
        this.ipAddress = ipAddress;
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and setters omitted for brevity

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public SecurityEventType getEventType() {
        return eventType;
    }

    public SecuritySeverity getSeverity() {
        return severity;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
