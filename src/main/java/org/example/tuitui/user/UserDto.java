package org.example.tuitui.user;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private boolean isMerchant;

    // 靜態工廠方法：快速將 Entity 轉為 DTO
    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(String.valueOf(user.getId()));
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setMerchant(user.isMerchant());
        return dto;
    }
}