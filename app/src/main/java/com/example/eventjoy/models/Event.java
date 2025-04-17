package com.example.eventjoy.models;

import com.example.eventjoy.enums.EventStatus;

import java.time.LocalDateTime;

public class Event extends DomainEntity{

    private String title;
    private LocalDateTime startDateAndTime;
    private LocalDateTime endDateAndTime;
    private String description;
    private int maxParticipants;
    private Address address;
    private EventStatus status;
    private String idCreator;
    private String idGroup;

    public Event() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartDateAndTime() {
        return startDateAndTime;
    }

    public void setStartDateAndTime(LocalDateTime startDateAndTime) {
        this.startDateAndTime = startDateAndTime;
    }

    public LocalDateTime getEndDateAndTime() {
        return endDateAndTime;
    }

    public void setEndDateAndTime(LocalDateTime endDateAndTime) {
        this.endDateAndTime = endDateAndTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getIdCreator() {
        return idCreator;
    }

    public void setIdCreator(String idCreator) {
        this.idCreator = idCreator;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", startDateAndTime=" + startDateAndTime +
                ", endDateAndTime=" + endDateAndTime +
                ", description='" + description + '\'' +
                ", maxParticipants=" + maxParticipants +
                ", address=" + address +
                ", status=" + status +
                ", idCreator='" + idCreator + '\'' +
                ", idGroup='" + idGroup + '\'' +
                '}';
    }
}
