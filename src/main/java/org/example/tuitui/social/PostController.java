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

            if (userIdStr == null || content == null) {
                return ResponseEntity.badRequest().body("userId and content are required");
            }

            // 👇 [關鍵修正] 不需要轉 Long 了，因為現在 ID 是 String (UUID)
            // Long userId = Long.parseLong(userIdStr); // 這一行已註解掉，避免報錯

            // A. 直接用字串 ID 找人
            // (注意：您的 UserRepository 必須已經修正為 JpaRepository<User, String>)
            User user = userRepository.findById(userIdStr)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // B. 建立貼文物件
            Post post = new Post(content, user);

            // C. 儲存
            postRepository.save(post);

            return ResponseEntity.ok(post);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. 看所有貼文
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
}