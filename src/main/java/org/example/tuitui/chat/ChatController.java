package org.example.tuitui.chat;

import org.example.tuitui.user.User;
import org.example.tuitui.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // 1. 建立或取得聊天室 (對應 ApiService.createChat)
    @PostMapping
    public ResponseEntity<?> createChat(@RequestBody Map<String, String> payload) {
        // 在真實專案中，這裡應該從 Token 取得 currentUserId
        // 為了簡化，我們先假設前端傳這兩個 ID 上來，或者先寫死第一個使用者測試
        // *請注意：您的前端 ApiService.createChat 目前只傳了 targetUserId*
        // *因此我們需要一個方式知道 "誰" 在呼叫 API。*
        // *暫時方案：我們預設跟 ID 為 "1" 或列表第一個使用者對話，或者前端需要多傳 userId*

        // 修正方案：為了讓您現在能跑，我們假設 "User A" 是資料庫裡的第一個人 (模擬當前登入者)
        // 實際專案請結合 Spring Security

        String targetId = payload.get("targetUserId");
        if (targetId == null) return ResponseEntity.badRequest().body("Target User ID required");

        // 模擬：獲取當前登入者 (這裡先寫死用 ID 查找，您測試時請確保此用戶存在)
        // 您可以暫時把這行改成您的測試帳號 ID
        User currentUser = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users in DB"));

        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // 檢查是否已存在
        return chatRepository.findExistingChat(currentUser, targetUser)
                .map(this::convertToDto) // 如果有，直接回傳
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    // 沒有則建立
                    ChatThread chat = new ChatThread();
                    chat.setUserA(currentUser);
                    chat.setUserB(targetUser);
                    chat.setLastMessage("開始聊天吧！");
                    chat.setLastMessageTime(LocalDateTime.now());
                    chatRepository.save(chat);
                    return ResponseEntity.ok(convertToDto(chat));
                });
    }

    // 2. 取得我的聊天列表 (對應 ApiService.fetchChatThreads)
    @GetMapping
    public List<Map<String, Object>> getMyChats() {
        // 同樣，暫時使用 "第一個用戶" 作為 "我"
        User currentUser = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users in DB"));

        return chatRepository.findByUser(currentUser).stream()
                .map(chat -> convertToDto(chat, currentUser)) // 傳入 currentUser 以便判斷誰是 "對方"
                .collect(Collectors.toList());
    }

    // 3. 取得訊息內容 (對應 ApiService.fetchMessages)
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

    // 4. 發送訊息 (對應 ApiService.sendMessage)
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String chatId, @RequestBody Map<String, String> payload) {
        ChatThread thread = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // 暫時模擬：假設發送者是 UserA (實際應從 Token 拿)
        // 為了讓您的前端 isMe 判斷生效，這裡最好能動態抓
        User sender = thread.getUserA();

        ChatMessage message = new ChatMessage();
        message.setThread(thread);
        message.setSender(sender);
        message.setContent(payload.get("content"));
        messageRepository.save(message);

        // 更新聊天室最後一句話
        thread.setLastMessage(payload.get("content"));
        thread.setLastMessageTime(LocalDateTime.now());
        chatRepository.save(thread);

        return ResponseEntity.ok().build();
    }

    // 輔助方法：轉成前端需要的 JSON 格式
    private Map<String, Object> convertToDto(ChatThread chat) {
        return convertToDto(chat, chat.getUserA()); // 預設視角
    }

    private Map<String, Object> convertToDto(ChatThread chat, User me) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", chat.getId());
        map.put("lastMessage", chat.getLastMessage());
        map.put("lastMessageTime", chat.getLastMessageTime() != null ? chat.getLastMessageTime().toString() : "");
        map.put("unreadCount", 0); // 暫時寫死

        // 判斷誰是 "對方" (Target User)
        User target = chat.getUserA().getId().equals(me.getId()) ? chat.getUserB() : chat.getUserA();

        Map<String, String> userMap = new HashMap<>();
        userMap.put("nickname", target.getNickname());
        userMap.put("avatarUrl", target.getAvatarUrl());
        map.put("targetUser", userMap);

        return map;
    }
}