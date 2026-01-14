package org.example.tuitui.chat;

import com.fasterxml.jackson.databind.ObjectMapper; // 1. 務必引入這個
import org.example.tuitui.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatStorageConsumer {

    @Autowired private MessageRepository messageRepository;
    @Autowired private ChatRepository chatRepository;
    @Autowired private UserRepository userRepository;

    // 使用手動建立的 ObjectMapper，避免依賴注入失敗
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 【關鍵修正】這裡接收型別必須改為 String (因為 Controller 送出的是 String)
    @KafkaListener(topics = "chat-storage", groupId = "tuitui-db-writer")
    @Transactional
    public void handleChatMessage(String messageJson) {
        try {
            System.out.println("Kafka 收到訊息字串: " + messageJson); // 加個 Log 確認有收到

            // 【關鍵修正】手動將 JSON 字串轉回 DTO 物件
            ChatMessageDto dto = objectMapper.readValue(messageJson, ChatMessageDto.class);

            // --- 以下邏輯保持不變 ---
            ChatThread thread = chatRepository.findById(dto.getThreadId()).orElse(null);
            if (thread == null) {
                System.err.println("找不到聊天室 ID: " + dto.getThreadId());
                return;
            }

            ChatMessage message = new ChatMessage();
            message.setThread(thread);
            message.setSender(userRepository.findById(dto.getSenderId()).orElse(null));
            message.setContent(dto.getContent());

            // 建議補上時間
            if (dto.getTimestamp() != null) {
                // message.setCreatedAt(LocalDateTime.parse(dto.getTimestamp())); // 若有需要精準時間
            }

            messageRepository.save(message);

            // 更新聊天室最後一句話
            thread.setLastMessage(dto.getContent());
            thread.setLastMessageTime(LocalDateTime.now());
            chatRepository.save(thread);

            System.out.println("✅ 訊息已成功寫入資料庫: " + dto.getContent());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ 訊息處理失敗: " + messageJson);
        }
    }
}