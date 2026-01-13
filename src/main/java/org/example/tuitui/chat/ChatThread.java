package org.example.tuitui.chat;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.tuitui.common.BaseEntity;
import org.example.tuitui.user.User;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "chat_threads")
public class ChatThread extends BaseEntity {

    // 聊天室的兩個參與者
    @ManyToOne
    @JoinColumn(name = "user_a_id")
    private User userA;

    @ManyToOne
    @JoinColumn(name = "user_b_id")
    private User userB;

    // 快取最後一條訊息，方便列表顯示
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}