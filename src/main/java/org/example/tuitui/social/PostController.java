package org.example.tuitui.social;

import org.example.tuitui.user.User;
import org.example.tuitui.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
// 👇【關鍵修正 1】改用 originPatterns，解決 CORS 報錯
@CrossOrigin(originPatterns = "*")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository; // 【關鍵修正 2】需要這個來查發文者

    // 1. 發布貼文 (POST /api/posts)
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> payload) {
        try {
            // 解析前端傳來的資料
            String userIdStr = payload.get("userId");
            String content = payload.get("content");

            if (userIdStr == null || content == null) {
                return ResponseEntity.badRequest().body("userId and content are required");
            }

            Long userId = Long.parseLong(userIdStr);

            // A. 先找出是誰發的文 (User)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // B. 建立貼文物件 (關聯 User)
            Post post = new Post(content, user);

            // C. 儲存到資料庫
            postRepository.save(post);

            return ResponseEntity.ok(post);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. 看所有貼文 (首頁動態牆)
    @GetMapping
    public List<Post> getAllPosts() {
        // 【關鍵修正 3】改用時間倒序，新貼文在上面
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
}