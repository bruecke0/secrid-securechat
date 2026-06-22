package com.sild.securechat_backend.auth.dto;

public record AuthResponse (
    Long userId,
    String username,
    String email,
    String role,
    String message
) {}
