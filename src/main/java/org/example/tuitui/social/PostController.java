package org.example.tuitui.social;

import org.example.tuitui.user.User;
import org.example.tuitui.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(originPatterns = "*")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. 取得所有貼文 (Feed)
    @GetMapping
    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 2. [修正] 取得特定用戶的貼文 (Profile)
    // ❌ 原本錯誤: Long.parseLong(userId)
    // ✅ 修正後: 直接使用 String userId
    @GetMapping("/user/{userId}")
    public List<PostDto> getUserPosts(@PathVariable String userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PostDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 3. 發布貼文
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> payload) {
        try {
            String userIdStr = payload.get("userId");
            String content = payload.get("content");

            if (userIdStr == null || content == null) {
                return ResponseEntity.badRequest().body("userId and content are required");
            }

            // ❌ 原本錯誤: userRepository.findById(Long.parseLong(userIdStr))
            // ✅ 修正後: 直接傳入 String
            User user = userRepository.findById(userIdStr)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Post post = new Post();
            post.setContent(content);
            post.setUser(user);

            // 時間部分：若 BaseEntity 有設定 @PrePersist，這裡其實可以省略，但手動設也無妨
            post.setCreatedAt(LocalDateTime.now());

            post = postRepository.save(post);

            return ResponseEntity.ok(PostDto.fromEntity(post));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}