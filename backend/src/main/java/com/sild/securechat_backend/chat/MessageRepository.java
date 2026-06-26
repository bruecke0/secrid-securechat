package com.sild.securechat_backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findTop50ByRoomAndDeletedFalseOrderByCreatedAtDesc(ChatRoom room);
}
