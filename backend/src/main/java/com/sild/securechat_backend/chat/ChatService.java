package com.sild.securechat_backend.chat;

import com.sild.securechat_backend.chat.dto.ChatRoomResponse;
import com.sild.securechat_backend.chat.dto.CreateRoomRequest;
import com.sild.securechat_backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final RoomMemberRepository roomMemberRepository;

    public ChatService(
            ChatRoomRepository chatRoomRepository,
            RoomMemberRepository roomMemberRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.roomMemberRepository = roomMemberRepository;
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
}