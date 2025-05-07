package com.example.eventjoy.models;

import java.time.LocalDateTime;

public class Invitation extends DomainEntity{

    private String invitedUserId;
    private String inviterUserId;
    private String groupId;
    private LocalDateTime invitedAt;

    public Invitation() {
        super();
    }

    public String getInvitedUserId() {
        return invitedUserId;
    }

    public void setInvitedUserId(String invitedUserId) {
        this.invitedUserId = invitedUserId;
    }

    public String getInviterUserId() {
        return inviterUserId;
    }

    public void setInviterUserId(String inviterUserId) {
        this.inviterUserId = inviterUserId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public LocalDateTime getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(LocalDateTime invitedAt) {
        this.invitedAt = invitedAt;
    }

    @Override
    public String toString() {
        return "Invitation{" +
                "invitedUserId='" + invitedUserId + '\'' +
                ", inviterUserId='" + inviterUserId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", invitedAt=" + invitedAt +
                '}';
    }
}
