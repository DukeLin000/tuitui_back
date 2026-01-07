package org.example.tuitui.social;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 【新增這行】允許前端跨域存取貼文 API
public class PostController {

    private final PostRepository postRepository;

    // 發布貼文
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        // 注意：這裡接收到的 post 物件，裡面的 images 必須手動關聯
        if (post.getImages() != null) {
            post.getImages().forEach(img -> img.setPost(post));
        }
        return postRepository.save(post);
    }

    // 看所有貼文 (首頁動態牆)
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
}