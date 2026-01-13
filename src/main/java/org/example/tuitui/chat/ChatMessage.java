package org.example.tuitui.chat;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.tuitui.common.BaseEntity;
import org.example.tuitui.user.User;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "thread_id")
    private ChatThread thread;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;
}