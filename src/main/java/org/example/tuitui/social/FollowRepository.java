package org.example.tuitui.social;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // ❌ 原本: (Long followerId, Long targetId)
    // ✅ 修正: (String followerId, String targetId)
    boolean existsByFollowerIdAndTargetId(String followerId, String targetId);

    void deleteByFollowerIdAndTargetId(String followerId, String targetId);

    long countByTargetId(String targetId);

    long countByFollowerId(String followerId);
}