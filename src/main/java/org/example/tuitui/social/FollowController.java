package org.example.tuitui.social;

import org.example.tuitui.user.User;
import org.example.tuitui.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(originPatterns = "*")
public class FollowController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. 追蹤 / 取消追蹤
    @PostMapping("/follow")
    @Transactional
    public ResponseEntity<?> toggleFollow(@RequestBody Map<String, String> payload) {
        try {
            // ❌ 錯誤寫法: Long myId = Long.parseLong(payload.get("myId"));
            // ✅ 正確寫法: 直接接字串 (因為 User ID 是 UUID String)
            String myId = payload.get("myId");
            String targetId = payload.get("targetId");

            if (myId == null || targetId == null) {
                return ResponseEntity.badRequest().body("IDs cannot be null");
            }
            if (myId.equals(targetId)) {
                return ResponseEntity.badRequest().body("Cannot follow yourself");
            }

            // 這裡 findById(String) 就不會報錯了
            boolean exists = followRepository.existsByFollowerIdAndTargetId(myId, targetId);

            if (exists) {
                // 退追
                followRepository.deleteByFollowerIdAndTargetId(myId, targetId);
                // updateUserCounts(myId, targetId); // 暫時註解，先求跑通
                return ResponseEntity.ok(Map.of("status", "unfollowed"));
            } else {
                // 追蹤
                User me = userRepository.findById(myId).orElseThrow(() -> new RuntimeException("Me not found"));
                User target = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("Target not found"));

                followRepository.save(new Follow(me, target));
                // updateUserCounts(myId, targetId);
                return ResponseEntity.ok(Map.of("status", "followed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 檢查是否追蹤 (給前端按鈕變色用)
    @GetMapping("/is-following")
    // ✅ 參數也要改成 String
    public ResponseEntity<?> checkFollow(@RequestParam String myId, @RequestParam String targetId) {
        boolean isFollowing = followRepository.existsByFollowerIdAndTargetId(myId, targetId);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }
}