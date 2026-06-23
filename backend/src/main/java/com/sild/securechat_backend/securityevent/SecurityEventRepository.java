package com.sild.securechat_backend.securityevent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    // Custom query methods can be added here if needed

    List<SecurityEvent> findTop50ByOrderByCreatedAtDesc();

    List<SecurityEvent> findByUserIdOrderByCreatedAtDesc(Long userId);
}
