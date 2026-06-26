package com.sild.securechat_backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
    @NotBlank(message = "Room name is required")
    @Size(min = 3, max = 80, message = "Room name must be between 3 and 80 characters")
    String name
) {
}

