package com.example.fitnessapp.model.response.community;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PostResponse implements Serializable {

    private Long id;
    private String title;
    private String name;
    private String content;
    private String imageUrl;
    private String videoUrl;

    private Long userId;
    private String userName;
    private String userAvatarUrl;

    private Long likeCount;
    private Long commentCount;

    private Boolean liked;
    private Boolean canEdit;
    private Boolean canDelete;

    private String createAt;

    public PostResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
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

    public Long getCommentCount() {
        return commentCount;
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

    public String getCreateAt() {
        return createAt;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return "PostResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userAvatarUrl='" + userAvatarUrl + '\'' +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", liked=" + liked +
                ", canEdit=" + canEdit +
                ", canDelete=" + canDelete +
                ", createAt=" + createAt +
                '}';
    }
}