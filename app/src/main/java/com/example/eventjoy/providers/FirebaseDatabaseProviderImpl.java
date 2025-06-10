package com.example.eventjoy.providers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseProviderImpl implements FirebaseDatabaseProvider {
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();

    public DatabaseReference getGroupsReference() {
        return db.getReference("groups");
    }

    public DatabaseReference getUserGroupsReference() {
        return db.getReference("userGroups");
    }

    public DatabaseReference getMessagesReference() {
        return db.getReference("messages");
    }

    public DatabaseReference getInvitationsReference() {
        return db.getReference("invitations");
    }

    public DatabaseReference getReportsReference() {
        return db.getReference("reports");
    }

    public DatabaseReference getEventsReference() {
        return db.getReference("events");
    }

    public DatabaseReference getUserEventsReference() {
        return db.getReference("userEvents");
    }
}