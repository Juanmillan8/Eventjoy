package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UserEventService {

    private DatabaseReference databaseReferenceUserEvent;

    public UserEventService(Context context) {
        databaseReferenceUserEvent = FirebaseDatabase.getInstance().getReference().child("userEvents");
    }

    public void checkMemberIsParticipant(String eventId, String userId, SimpleCallback callback) {
        databaseReferenceUserEvent.orderByChild("eventId").equalTo(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserEvent userEvent = snapshot.getValue(UserEvent.class);
                        if (userEvent.getUserId().equals(userId)) {
                            callback.onSuccess("Participant");
                            return;
                        }
                    }
                    callback.onSuccess("NoParticipant");
                } else {
                    callback.onSuccess("NoParticipant");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - UserEventService - checkMemberIsParticipant", error.getMessage());
                callback.onCancelled("Error querying the database: " + error.getMessage());
            }
        });
    }

    public void getByEventId(String eventId, Boolean listenForChanges, ValueEventListener listener) {
        Query query = databaseReferenceUserEvent.orderByChild("eventId").equalTo(eventId);
        if (listenForChanges) {
            query.addValueEventListener(listener);
        } else {
            query.addListenerForSingleValueEvent(listener);
        }
    }

    public void leaveEvent(String eventId, String userId, SimpleCallback callback) {
        databaseReferenceUserEvent.orderByChild("eventId").equalTo(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserEvent userEvent = new UserEvent();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    userEvent = snapshot.getValue(UserEvent.class);
                    if (userEvent.getUserId().equals(userId)) {
                        break;
                    }
                }
                databaseReferenceUserEvent.child(userEvent.getId()).removeValue();
                callback.onSuccess("You are no longer part of this event");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - UserEventService - leaveEvent", error.getMessage());
                callback.onCancelled("Error querying the database: " + error.getMessage());
            }
        });
    }

    public String joinTheEvent(UserEvent ue) {
        DatabaseReference newReference = databaseReferenceUserEvent.push();
        ue.setId(newReference.getKey());

        newReference.setValue(ue);
        return ue.getId();
    }

}
