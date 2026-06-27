package com.sild.securechat_backend.chat;

import com.sild.securechat_backend.chat.dto.ChatRoomResponse;
import com.sild.securechat_backend.chat.dto.CreateRoomRequest;
import com.sild.securechat_backend.user.User;
import com.sild.securechat_backend.chat.dto.CreateMessageRequest;
import com.sild.securechat_backend.chat.dto.MessageResponse;
import com.sild.securechat_backend.securityevent.SecurityEventService;
import com.sild.securechat_backend.securityevent.SecurityEventType;
import com.sild.securechat_backend.securityevent.SecuritySeverity;
import com.sild.securechat_backend.chat.dto.RoomMemberResponse;
import com.sild.securechat_backend.chat.dto.ActionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Collections;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final MessageRepository messageRepository;
    private final SecurityEventService securityEventService;

    public ChatService(
            ChatRoomRepository chatRoomRepository,
            RoomMemberRepository roomMemberRepository,
            MessageRepository messageRepository,
            SecurityEventService securityEventService
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.messageRepository = messageRepository;
        this.securityEventService = securityEventService;
    }

    @Transactional
    public ChatRoomResponse createRoom(CreateRoomRequest request, User currentUser) {
        ChatRoom room = new ChatRoom(
                request.name(),
                ChatRoomType.GROUP,
                currentUser
        );

        ChatRoom savedRoom = chatRoomRepository.save(room);

        RoomMember ownerMembership = new RoomMember(
                savedRoom,
                currentUser,
                RoomMemberRole.OWNER
        );

        RoomMember savedMembership = roomMemberRepository.save(ownerMembership);

        return mapToRoomResponse(savedMembership);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(User currentUser) {
        return roomMemberRepository.findByUserOrderByJoinedAtDesc(currentUser)
                .stream()
                .map(this::mapToRoomResponse)
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long roomId, CreateMessageRequest request, User currentUser) {
        ChatRoom room = getRoomOrThrow(roomId);

        if (!roomMemberRepository.existsByRoomAndUser(room, currentUser)) {
            logRoomAccessDenied(roomId, currentUser);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }

        Message message = new Message(
            room,
            currentUser,
            request.content().trim()
        );

        Message savedMessage = messageRepository.save(message);

        securityEventService.logEvent(
            currentUser.getId(),
            SecurityEventType.MESSAGE_SENT,
            SecuritySeverity.LOW,
            null,
            "Message sent in room id: " + roomId
        );

        return mapToMessageResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long roomId, User currentUser) {
        ChatRoom room = getRoomOrThrow(roomId);

        if (!roomMemberRepository.existsByRoomAndUser(room, currentUser)) {
            logRoomAccessDenied(roomId, currentUser);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this room");   
        }

        List<Message> messages = messageRepository.findTop50ByRoomAndDeletedFalseOrderByCreatedAtDesc(room);

        Collections.reverse(messages);

        return messages.stream()
            .map(this::mapToMessageResponse)
            .toList();
    }

    @Transactional
    public ChatRoomResponse joinRoom(Long roomId, User currentUser) {
        ChatRoom room = getRoomOrThrow(roomId);

        if (roomMemberRepository.existsByRoomAndUser(room, currentUser)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already a member of this room");
        }

        RoomMember membership = new RoomMember(
            room, 
            currentUser,
            RoomMemberRole.MEMBER
        );

        RoomMember savedMembership = roomMemberRepository.save(membership);

        return mapToRoomResponse(savedMembership);
    }

    @Transactional(readOnly = true)
    public List<RoomMemberResponse> getRoomMembers(Long roomId, User currentUser) {
        ChatRoom room = getRoomOrThrow(roomId);

        if (!roomMemberRepository.existsByRoomAndUser(room, currentUser)) {
            logRoomAccessDenied(roomId, currentUser);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }

        return roomMemberRepository.findByRoomOrderByJoinedAtAsc(room)
            .stream()
            .map(this::mapToRoomMemberResponse)
            .toList();
    }

    @Transactional
    public ActionResponse leaveRoom(Long roomId, User currentUser) {
        ChatRoom room = getRoomOrThrow(roomId);

        RoomMember membership = roomMemberRepository.findByRoomAndUser(room, currentUser)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this room"));
        
        if (membership.getRole() == RoomMemberRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room owner cannot leave the room yet");
        }

        roomMemberRepository.delete(membership);

        return new ActionResponse("You left the room");
    }

    @Transactional
    public ActionResponse removeMember(Long roomId, Long targetUserId, User currentUser) {
        ChatRoom room = getRoomOrThrow(roomId);

        RoomMember currentUserMembership = roomMemberRepository.findByRoomAndUser(room, currentUser)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this room"));

        if (currentUserMembership.getRole() != RoomMemberRole.OWNER) {
            logRoomAccessDenied(roomId, currentUser);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the room owner can remove members");
        }

        if (currentUser.getId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner cannot remove themselves");
        }

        RoomMember targetMembership = roomMemberRepository.findByRoomAndUserId(room, targetUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user is not a member of this room"));

        if (targetMembership.getRole() == RoomMemberRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove the room owner");
        }

        roomMemberRepository.delete(targetMembership);

        return new ActionResponse("Member removed from room");
    }

    private ChatRoom getRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    private void logRoomAccessDenied(Long roomId, User currentUser) {
        securityEventService.logEvent(
            currentUser.getId(),
            SecurityEventType.ROOM_JOIN_DENIED,
            SecuritySeverity.MEDIUM,
            null,
            "User tried to access room without membership. Room id: " + roomId
        );
    }

    private ChatRoomResponse mapToRoomResponse(RoomMember roomMember) {
        ChatRoom room = roomMember.getRoom();

        return new ChatRoomResponse(
                room.getId(),
                room.getName(),
                room.getType().name(),
                roomMember.getRole().name(),
                room.getCreatedBy().getId(),
                room.getCreatedAt()
        );
    }

    private MessageResponse mapToMessageResponse(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getRoom().getId(),
            message.getSender().getId(),
            message.getSender().getUsername(),
            message.getContent(),
            message.getCreatedAt()
        );
    }

    private RoomMemberResponse mapToRoomMemberResponse(RoomMember roomMember) {
        return new RoomMemberResponse(
            roomMember.getUser().getId(),
            roomMember.getUser().getUsername(),
            roomMember.getRole().name(),
            roomMember.getJoinedAt()
        );
    }
}