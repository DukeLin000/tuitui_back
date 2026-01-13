package org.example.tuitui.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, String> {
    // 根據聊天室 ID 抓取訊息 (依照時間排序)
    List<ChatMessage> findByThreadIdOrderByCreatedAtAsc(String threadId);
}