package com.example.eventjoy.models;

import java.time.LocalDateTime;

public class Message extends DomainEntity{

    private LocalDateTime sentAt;
    private String content;
    private String senderUserId;
    private String groupId;

    public Message() {
        super();
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sentAt=" + sentAt +
                ", content='" + content + '\'' +
                ", senderUserId='" + senderUserId + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
