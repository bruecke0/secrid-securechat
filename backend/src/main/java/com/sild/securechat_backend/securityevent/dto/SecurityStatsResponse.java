package com.sild.securechat_backend.securityevent.dto;

public record SecurityStatsResponse(
    long loginSuccessCount,
    long loginFailedCound,
    long accountLockedCount,
    long registerSuccessCount,
    long highSeverityCount,
    long criticalSeverityCount
) {}
