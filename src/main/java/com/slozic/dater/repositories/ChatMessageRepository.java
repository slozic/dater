package com.slozic.dater.repositories;

import com.slozic.dater.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findAllByDateIdOrderByCreatedAtAsc(UUID dateId);
}
