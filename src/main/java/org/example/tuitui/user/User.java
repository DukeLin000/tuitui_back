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

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String password;

    @Column(length = 1000)
    private String avatarUrl;

    private String bio;

    @Column(nullable = false)
    private Integer followingCount = 0;

    @Column(nullable = false)
    private Integer followerCount = 0;

    @Column(nullable = false)
    private Integer likeCount = 0;

    private boolean isVerified;

    // 👇 [新增] 這行就是缺失的部分
    // Lombok 會自動為 boolean 生成 isMerchant() 方法
    private boolean isMerchant = false;
}