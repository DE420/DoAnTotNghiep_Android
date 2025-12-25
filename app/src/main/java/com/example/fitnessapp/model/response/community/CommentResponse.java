package com.example.fitnessapp.model.response.community;

import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private String content;
    private String imageUrl;

    private Long userId;
    private String userName;
    private String userAvatarUrl;

    private Long likeCount;
    private Boolean liked;

    private Boolean canEdit;
    private Boolean canDelete;



    private String createdAt;

    public CommentResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public Boolean getLiked() {
        return liked;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "CommentResponse{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userAvatarUrl='" + userAvatarUrl + '\'' +
                ", likeCount=" + likeCount +
                ", liked=" + liked +
                ", canEdit=" + canEdit +
                ", canDelete=" + canDelete +
                ", createdAt=" + createdAt +
                '}';
    }
}
