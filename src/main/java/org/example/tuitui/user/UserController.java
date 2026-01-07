package org.example.tuitui.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 告訴 Spring 這是一個 REST API
@RequestMapping("/api/users") // API 的根路徑
@RequiredArgsConstructor // Lombok 神技：自動幫 final 變數生成建構子 (注入 Repository)
@CrossOrigin(origins = "*") // 【新增這行】解決 CORS 跨域問題，允許前端呼叫
public class UserController {

    private final UserRepository userRepository;

    // 1. 建立新用戶 (POST /api/users)
    @PostMapping
    public User createUser(@RequestBody User user) {
        // 在真實專案這裡應該要加密碼 hash，MVP 先直接存
        return userRepository.save(user);
    }

    // 2. 查詢所有用戶 (GET /api/users)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}