package com.sild.securechat_backend.chat.dto;

import java.time.LocalDateTime;

public record RoomMemberResponse(
    Long userId, 
    String username,
    String role,
    LocalDateTime joinedAt
) {}
