package com.sild.securechat_backend.chat;

import com.sild.securechat_backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.cglib.core.Local;

@Entity
@Table(
    name = "room_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "user_id"})
    }
)
public class RoomMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomMemberRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public RoomMember(){
    }

    public RoomMember(ChatRoom room, User user, RoomMemberRole role) {
        this.room = room;
        this.user = user;
        this.role = role;
    }

    @PrePersist
    public void onCreate() {
        if (joinedAt == null){
            joinedAt = LocalDateTime.now();
        }

        if (role == null) {
            role = RoomMemberRole.MEMBER;
        }
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public User getUser() {
        return user;
    }

    public RoomMemberRole getRole() {
        return role;
    }

    public void setRole(RoomMemberRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
