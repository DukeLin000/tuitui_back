package org.example.tuitui.chat;

import com.fasterxml.jackson.databind.ObjectMapper; // 引入 Jackson
import lombok.SneakyThrows; // 簡化例外處理
import org.example.tuitui.user.User;
import org.example.tuitui.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(originPatterns = "*")
public class ChatController {

    @Autowired private ChatRepository chatRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;

    // 注入 WebSocket 推送工具
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // 注入 Kafka 生產者 (使用 String, String 避開序列化地獄)
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;

    // =========================================================================
    //  [關鍵修正] 方案 B：手動初始化 ObjectMapper
    //  不使用 @Autowired，直接 new 出來，解決依賴注入失敗的問題
    // =========================================================================
    private final ObjectMapper objectMapper = new ObjectMapper();

    // =================================================================================
    //  1. 既有 REST API (HTTP) - 保持不變
    // =================================================================================

    // 1. 建立或取得聊天室
    @PostMapping
    public ResponseEntity<?> createChat(@RequestBody Map<String, String> payload) {
        String targetId = payload.get("targetUserId");
        if (targetId == null) return ResponseEntity.badRequest().body("Target User ID required");

        User currentUser = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users in DB"));

        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        return chatRepository.findExistingChat(currentUser, targetUser)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    ChatThread chat = new ChatThread();
                    chat.setUserA(currentUser);
                    chat.setUserB(targetUser);

                    // [修改] 提取歡迎訊息為變數，供 Thread 與 Message 共用
                    String welcomeText = "開始聊天吧！";
                    chat.setLastMessage(welcomeText);
                    chat.setLastMessageTime(LocalDateTime.now());
                    chatRepository.save(chat);

                    // [新增] 修正聊天室空白問題：建立聊天室時，同步寫入第一則訊息至 Message 表
                    ChatMessage systemMsg = new ChatMessage();
                    systemMsg.setThread(chat);
                    systemMsg.setSender(currentUser); // 預設由發起人傳送
                    systemMsg.setContent(welcomeText);
                    messageRepository.save(systemMsg);

                    return ResponseEntity.ok(convertToDto(chat));
                });
    }

    // 2. 取得我的聊天列表
    @GetMapping
    public List<Map<String, Object>> getMyChats() {
        User currentUser = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users in DB"));

        return chatRepository.findByUser(currentUser).stream()
                .map(chat -> convertToDto(chat, currentUser))
                .collect(Collectors.toList());
    }

    // 3. 取得訊息內容
    @GetMapping("/{chatId}/messages")
    public List<Map<String, Object>> getMessages(@PathVariable String chatId) {
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(chatId).stream()
                .map(msg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", msg.getId());
                    map.put("content", msg.getContent());
                    map.put("senderId", msg.getSender().getId());
                    map.put("createdAt", msg.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // 4. 發送訊息 (HTTP版 - Fallback)
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String chatId, @RequestBody Map<String, String> payload) {
        ChatThread thread = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // [修改] 修正發送者身分錯誤的問題
        // 舊邏輯：User sender = thread.getUserA(); (若只保留這行，會導致 B 發的訊息也變成 A 發的)
        // 新邏輯：優先從 payload 獲取 senderId

        User sender;
        String senderId = payload.get("senderId");

        if (senderId != null) {
            sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("Sender user not found"));
        } else {
            // 若前端沒傳 senderId，則使用舊有邏輯作為 Fallback
            sender = thread.getUserA();
        }

        ChatMessage message = new ChatMessage();
        message.setThread(thread);
        message.setSender(sender);
        message.setContent(payload.get("content"));
        messageRepository.save(message);

        thread.setLastMessage(payload.get("content"));
        thread.setLastMessageTime(LocalDateTime.now());
        chatRepository.save(thread);

        return ResponseEntity.ok().build();
    }

    // =================================================================================
    //  2. WebSocket + Kafka 即時通訊區塊 (Hot Path)
    // =================================================================================

    @SneakyThrows
    @MessageMapping("/chat/{chatId}/sendMessage")
    public void processMessage(@DestinationVariable String chatId, @Payload Map<String, String> payload) {
        String content = payload.get("content");
        String senderId = payload.get("senderId");

        // 1. 構建 DTO
        ChatMessageDto msgDto = new ChatMessageDto();
        msgDto.setThreadId(chatId);
        msgDto.setSenderId(senderId);
        msgDto.setContent(content);
        msgDto.setTimestamp(LocalDateTime.now().toString());

        // 2. [Hot Path] WebSocket 及時推送
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, msgDto);

        // 3. [Cold Path] 轉成 JSON String 後送入 Kafka
        // 這裡會使用我們手動 new 出來的 objectMapper，保證不會 NullPointerException
        String jsonMessage = objectMapper.writeValueAsString(msgDto);
        kafkaTemplate.send("chat-storage", jsonMessage);

        System.out.println("WebSocket 訊息已處理並發送至 Kafka: " + content);
    }

    // =================================================================================
    //  輔助方法
    // =================================================================================

    private Map<String, Object> convertToDto(ChatThread chat) {
        return convertToDto(chat, chat.getUserA());
    }

    private Map<String, Object> convertToDto(ChatThread chat, User me) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", chat.getId());
        map.put("lastMessage", chat.getLastMessage());
        map.put("lastMessageTime", chat.getLastMessageTime() != null ? chat.getLastMessageTime().toString() : "");
        map.put("unreadCount", 0);

        User target = chat.getUserA().getId().equals(me.getId()) ? chat.getUserB() : chat.getUserA();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("nickname", target.getNickname());
        userMap.put("avatarUrl", target.getAvatarUrl());
        map.put("targetUser", userMap);

        return map;
    }
}