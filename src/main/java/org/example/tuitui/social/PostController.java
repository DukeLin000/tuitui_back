package org.example.tuitui.social;

import org.example.tuitui.user.User;
import org.example.tuitui.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(originPatterns = "*")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. 發布貼文 (POST /api/posts)
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> payload) {
        try {
            String userIdStr = payload.get("userId");
            String content = payload.get("content");

            // 基本驗證
            if (userIdStr == null || content == null) {
                return ResponseEntity.badRequest().body("userId and content are required");
            }

            // 1. 查找使用者 (使用 UUID String)
            User user = userRepository.findById(userIdStr)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. 建立貼文
            Post post = new Post(content, user);

            // 3. 補上時間 (確保排序正確，新貼文在最上面)
            LocalDateTime now = LocalDateTime.now();
            if (post.getCreatedAt() == null) {
                post.setCreatedAt(now);
            }
            if (post.getUpdatedAt() == null) {
                post.setUpdatedAt(now);
            }

            // 4. 存檔並強制寫入 (Flush) 以便立即取得 DB 狀態
            post = postRepository.saveAndFlush(post);

            // 5. [關鍵修正] 手動回填 userId
            // 因為 Post 實體的 userId 欄位被設為唯讀 (insertable=false)，
            // Hibernate 存檔後不會自動更新 Java 物件裡的 userId。
            // 我們必須手動填入，前端才能收到正確的 JSON 並判斷 "這是我的貼文"。
            post.setUserId(user.getId());

            return ResponseEntity.ok(post);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. 看所有貼文 (依照時間倒序)
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
}