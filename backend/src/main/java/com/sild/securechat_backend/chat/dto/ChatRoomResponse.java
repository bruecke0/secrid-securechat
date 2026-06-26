package com.sild.securechat_backend.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomResponse(
    Long id,
    String name, 
    String type,
    String memberRole,
    Long createdByUserId,
    LocalDateTime createdAt
) {
}
