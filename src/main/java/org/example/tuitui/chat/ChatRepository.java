package org.example.tuitui.chat;

import org.example.tuitui.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatThread, String> {

    // 找出這兩個人的對話 (不管 A 對 B 還是 B 對 A)
    @Query("SELECT c FROM ChatThread c WHERE (c.userA = :u1 AND c.userB = :u2) OR (c.userA = :u2 AND c.userB = :u1)")
    Optional<ChatThread> findExistingChat(@Param("u1") User user1, @Param("u2") User user2);

    // 找出某人參與的所有聊天室
    @Query("SELECT c FROM ChatThread c WHERE c.userA = :user OR c.userB = :user ORDER BY c.lastMessageTime DESC")
    List<ChatThread> findByUser(@Param("user") User user);
}