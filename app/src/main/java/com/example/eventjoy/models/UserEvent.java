package com.example.eventjoy.models;

public class UserEvent extends DomainEntity{

    private String userId;
    private String groupId;
    private Boolean notificationsEnabled;

    public UserEvent() {
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

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "userId='" + userId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", notificationsEnabled=" + notificationsEnabled +
                '}';
    }
}
