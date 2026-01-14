package org.example.tuitui.chat;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String threadId;
    private String senderId;
    private String content;
    private String timestamp;
}