package com.example.eventjoy.callbacks;

import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;

import java.util.List;

public interface EventsCallback {
    void onSuccess(List<Event> events);
    void onFailure(Exception e);
}
