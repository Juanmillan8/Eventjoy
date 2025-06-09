package com.example.eventjoy.models;

public class UserEvent extends DomainEntity{

    private String userId;
    private String eventId;

    public UserEvent() {
        super();
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


}
