package com.example.eventjoy.providers;

import com.google.firebase.database.DatabaseReference;

public interface FirebaseDatabaseProvider {

    DatabaseReference getGroupsReference();
    DatabaseReference getUserGroupsReference();
    DatabaseReference getMessagesReference();
    DatabaseReference getInvitationsReference();
    DatabaseReference getReportsReference();
    DatabaseReference getEventsReference();
    DatabaseReference getUserEventsReference();

}
