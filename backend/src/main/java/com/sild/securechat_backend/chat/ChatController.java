package com.sild.securechat_backend.chat;

import com.sild.securechat_backend.chat.dto.ChatRoomResponse;
import com.sild.securechat_backend.chat.dto.CreateRoomRequest;
import com.sild.securechat_backend.chat.dto.CreateMessageRequest;
import com.sild.securechat_backend.chat.dto.MessageResponse;
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
import org.springframework.web.bind.annotation.PathVariable;

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

    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(
        @PathVariable Long roomId,
        @Valid @RequestBody CreateMessageRequest request,
        Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        return chatService.sendMessage(roomId, request, currentUser);
    }

    @GetMapping("/{roomId}/messages")
    public List<MessageResponse> getMessages(
        @PathVariable Long roomId,
        Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        return chatService.getMessages(roomId, currentUser);
    }
}