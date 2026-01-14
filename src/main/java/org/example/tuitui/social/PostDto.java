package org.example.tuitui.social;

import lombok.Data;
import org.example.tuitui.user.UserDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PostDto {
    private String id;
    private String content;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private UserDto author; // 這裡只放 DTO，不放整個 User Entity

    public static PostDto fromEntity(Post post) {
        PostDto dto = new PostDto();
        dto.setId(String.valueOf(post.getId()));
        dto.setContent(post.getContent());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setCreatedAt(post.getCreatedAt());

        // 處理圖片
        if (post.getImages() != null) {
            dto.setImageUrls(post.getImages().stream()
                    .map(PostImage::getImageUrl)
                    .collect(Collectors.toList()));
        }

        // 處理作者
        if (post.getUser() != null) {
            dto.setAuthor(UserDto.fromEntity(post.getUser()));
        }
        return dto;
    }
}