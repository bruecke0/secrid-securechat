package com.sild.securechat_backend.securityevent.dto;

import java.time.LocalDateTime;

public record SecurityEventResponse (
    Long id, 
    Long userId,
    String eventType,
    String severity,
    String ipAddress,
    String details,
    LocalDateTime createdAt
){}
