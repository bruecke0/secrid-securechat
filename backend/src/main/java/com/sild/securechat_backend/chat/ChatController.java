package com.sild.securechat_backend.chat;

import com.sild.securechat_backend.chat.dto.ChatRoomResponse;
import com.sild.securechat_backend.chat.dto.CreateRoomRequest;
import com.sild.securechat_backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomResponse createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        return chatService.createRoom(request, currentUser);
    }

    @GetMapping
    public List<ChatRoomResponse> getMyRooms(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        return chatService.getMyRooms(currentUser);
    }
}