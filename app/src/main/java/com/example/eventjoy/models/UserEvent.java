package com.example.eventjoy.models;

public class UserEvent extends DomainEntity{

    private String userId;
    private String eventId;
    private Boolean isCreator;
    private Boolean notificationsEnabled;

    public UserEvent() {
        super();
    }

    public Boolean getCreator() {
        return isCreator;
    }

    public void setCreator(Boolean creator) {
        isCreator = creator;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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
                ", eventId='" + eventId + '\'' +
                ", isCreator=" + isCreator +
                ", notificationsEnabled=" + notificationsEnabled +
                '}';
    }
}
