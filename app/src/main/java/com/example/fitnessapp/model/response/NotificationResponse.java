package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class NotificationResponse implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("type")
    private String type;

    @SerializedName("isRead")
    private boolean isRead;

    @SerializedName("referenceId")
    private Long referenceId;

    @SerializedName("referenceUrl")
    private String referenceUrl;

    @SerializedName("createdAt")
    private String createdAt; // Format: "dd/MM/yyyy HH:mm:ss"

    // Constructors
    public NotificationResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getReferenceUrl() { return referenceUrl; }
    public void setReferenceUrl(String referenceUrl) { this.referenceUrl = referenceUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
