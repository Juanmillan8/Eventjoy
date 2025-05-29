package com.example.eventjoy.services;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EventService {

    private DatabaseReference databaseReferenceEvent;

    public EventService(Context context) {
        databaseReferenceEvent = FirebaseDatabase.getInstance().getReference().child("events");
    }

    public void checkOngoingEvent(String groupId, ValueEventListener listener) {
        Query query = databaseReferenceEvent.orderByChild("groupId").equalTo(groupId);
        query.addListenerForSingleValueEvent(listener);
    }
}
