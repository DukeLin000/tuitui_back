package org.example.tuitui.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.tuitui.common.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    // --- 帳號資訊 ---

    @Column(nullable = false, unique = true)
    private String username; // e.g. "@tuituiuser" (唯一 ID)

    // 【新增】Email 欄位 (前端註冊用)
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname; // e.g. "推推用戶" (顯示名稱)

    // 【修改】建議將 passwordHash 改名為 password
    // 這樣前端傳來的 JSON { "password": "..." } 才能自動寫入
    private String password;

    @Column(length = 1000)
    private String avatarUrl; // 頭像圖片連結

    private String bio; // 自介 (Bio)

    // --- 統計數據 (對應 Figma 上的數字) ---
    // 預設為 0，避免 null

    @Column(nullable = false)
    private Integer followingCount = 0; // 關注 (128)

    @Column(nullable = false)
    private Integer followerCount = 0;  // 粉絲 (1.2K)

    @Column(nullable = false)
    private Integer likeCount = 0;      // 獲讚 (86)

    // --- 狀態 ---
    private boolean isVerified; // 是否有藍勾勾TEST
}