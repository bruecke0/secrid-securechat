package com.sild.securechat_backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message cannot be longer than 2000 characters")
    String content
){}
    

