package com.sild.securechat_backend.auth.dto;

public record LoginResponse (
    Long userId,
    String username,
    String email,
    String role,
    String token,
    String tokenType,
    String message
) {}
