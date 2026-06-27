package com.sild.securechat_backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sild.securechat_backend.user.User;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    List<RoomMember> findByUserOrderByJoinedAtDesc(User user);

    List<RoomMember> findByRoomOrderByJoinedAtAsc(ChatRoom room);

    Optional<RoomMember> findByRoomAndUser(ChatRoom room, User user);

    boolean existsByRoomAndUser(ChatRoom room, User user);
}
