package org.example.tuitui.social;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 取得所有貼文 (時間倒序)
    List<Post> findAllByOrderByCreatedAtDesc();

    // [新增] 取得特定使用者的貼文 (時間倒序)
    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
}