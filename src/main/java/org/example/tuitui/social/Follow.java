package org.example.tuitui.social;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tuitui.user.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "target_id"}) // 防止重複追蹤
})
@Data
@NoArgsConstructor
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower; // 誰按了追蹤 (我)

    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false)
    private User target;   // 追蹤了誰 (對方)

    private LocalDateTime createdAt = LocalDateTime.now();

    public Follow(User follower, User target) {
        this.follower = follower;
        this.target = target;
    }
}