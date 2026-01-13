package org.example.tuitui.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 註冊邏輯
    public User register(String email, String password, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email 已經被註冊過了！");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname);

        String baseName = email.split("@")[0];
        user.setUsername("@" + baseName + "_" + UUID.randomUUID().toString().substring(0, 4));

        user.setAvatarUrl("https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100");
        user.setBio("這是一個新用戶");
        user.setFollowingCount(0);
        user.setFollowerCount(0);
        user.setLikeCount(0);
        user.setVerified(false);
        user.setMerchant(false);

        return userRepository.save(user);
    }

    // 登入邏輯
    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("找不到此用戶");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密碼錯誤");
        }
        return user;
    }

    // 取得個人資料
    // 👇 [修正] 這裡原本是 Long id，改成 String id
    public User getUserProfile(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
    }
}