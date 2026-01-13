package org.example.tuitui.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
// 👇【關鍵修正】改用 originPatterns，這樣搭配 allowCredentials 就不會報錯了
@CrossOrigin(originPatterns = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. 註冊 API
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.register(
                    payload.get("email"),
                    payload.get("password"),
                    payload.get("name")
            );
            return ResponseEntity.ok(convertToDto(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. 登入 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.login(
                    payload.get("email"),
                    payload.get("password")
            );
            return ResponseEntity.ok(convertToDto(user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // 3. 取得個人資料
    @GetMapping("/user/{id}")
    // 👇 [修正] 這裡原本是 @PathVariable Long id，改成 String id
    public ResponseEntity<?> getUser(@PathVariable String id) {
        try {
            return ResponseEntity.ok(convertToDto(userService.getUserProfile(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> convertToDto(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("name", user.getNickname());
        dto.put("avatar", user.getAvatarUrl());
        dto.put("bio", user.getBio());
        dto.put("role", user.isMerchant() ? "merchant" : "user");
        dto.put("username", user.getUsername());
        return dto;
    }
}