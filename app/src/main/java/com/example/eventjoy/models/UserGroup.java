package com.example.eventjoy.models;

import java.time.LocalDateTime;

public class UserGroup extends DomainEntity{

    private String userId;
    private String groupId;
    private Boolean admin;
    private String joinedAt;
    private Boolean notificationsEnabled;

    public UserGroup() {
        super();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    @Override
    public String toString() {
        return "UserGroup{" +
                "userId='" + userId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", admin=" + admin +
                ", joinedAt='" + joinedAt + '\'' +
                ", notificationsEnabled=" + notificationsEnabled +
                '}';
    }
}
