package com.slozic.dater.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "date_id")
    private UUID dateId;

    @NotNull
    @Column(name = "sender_id")
    private UUID senderId;

    @NotNull
    @Column(name = "recipient_id")
    private UUID recipientId;

    @NotBlank
    private String message;

    @NotNull
    @Builder.Default
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
