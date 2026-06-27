package com.sild.securechat_backend.chat.dto;

import java.time.LocalDateTime;

public record MessageResponse(
    Long id,
    Long roomId,
    Long senderId,
    String senderUsername,
    String content,
    LocalDateTime created_at
) {}
