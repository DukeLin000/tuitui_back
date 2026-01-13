package org.example.tuitui.social;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.tuitui.common.BaseEntity;
import org.example.tuitui.user.User; // [新增] 引入 User

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    // [修改] 將 userId 設為唯讀映射
    // 這樣可以保留這個欄位給舊程式讀取，但寫入資料庫時由下方的 User 物件負責
    @Column(name = "user_id", insertable = false, updatable = false)
    private String userId;

    // [新增] 真正的關聯：多對一 (多篇貼文對應一個用戶)
    // 這裡負責管理 user_id 這個外鍵
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String locationName;

    private Integer likeCount = 0;
    private Integer commentCount = 0;

    // 保留原有的 PostImage 設定 (急切加載)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<PostImage> images = new ArrayList<>();

    // --- [新增] 建構子 (為了讓 Controller 可以方便建立物件) ---
    public Post() {
    }

    public Post(String content, User user) {
        this.content = content;
        this.user = user;
    }
    // -----------------------------------------------------

    public void addImage(String url) {
        PostImage image = new PostImage();
        image.setImageUrl(url);
        image.setSortOrder(this.images.size());
        image.setPost(this);
        this.images.add(image);
    }
}