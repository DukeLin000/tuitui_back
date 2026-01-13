package org.example.tuitui.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// 👇 [關鍵修正] 將 <User, Long> 改為 <User, String>
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}