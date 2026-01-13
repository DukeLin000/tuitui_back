package org.example.tuitui.social;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// 1. 將第二個泛型參數從 String 改為 Long (對應 Post 的 @Id 型別)
public interface PostRepository extends JpaRepository<Post, Long> {

    // 2. [新增] 查詢所有貼文，並依照建立時間倒序 (新貼文在上面)
    // 這是給首頁動態牆 (PostController.getAllPosts) 用的
    List<Post> findAllByOrderByCreatedAtDesc();

    // 3. 查詢某個人的所有貼文 (個人頁用)
    // 雖然現在 Post 裡面有 User 物件，但因為我們保留了 userId 欄位，所以這行依然有效
    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
}