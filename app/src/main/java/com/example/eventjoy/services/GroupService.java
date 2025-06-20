package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.providers.FirebaseDatabaseProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GroupService {

    private DatabaseReference databaseReferenceGroups;
    private DatabaseReference databaseReferenceUserGroups;
    private DatabaseReference databaseReferenceMessage;
    private DatabaseReference databaseReferenceInvitation;
    private DatabaseReference databaseReferenceReport;
    private DatabaseReference databaseReferenceEvent;
    private DatabaseReference databaseReferenceUserEvents;

    public GroupService(Context context) {
        databaseReferenceGroups = FirebaseDatabase.getInstance().getReference().child("groups");
        databaseReferenceUserGroups = FirebaseDatabase.getInstance().getReference().child("userGroups");
        databaseReferenceMessage = FirebaseDatabase.getInstance().getReference().child("messages");
        databaseReferenceInvitation = FirebaseDatabase.getInstance().getReference().child("invitations");
        databaseReferenceReport = FirebaseDatabase.getInstance().getReference().child("reports");
        databaseReferenceEvent = FirebaseDatabase.getInstance().getReference().child("events");
        databaseReferenceUserEvents = FirebaseDatabase.getInstance().getReference().child("userEvents");
    }

    public GroupService(FirebaseDatabase firebaseDatabase) {
        databaseReferenceGroups = firebaseDatabase.getReference().child("groups");
        databaseReferenceUserGroups = firebaseDatabase.getReference().child("userGroups");
        databaseReferenceMessage = firebaseDatabase.getReference().child("messages");
        databaseReferenceInvitation = firebaseDatabase.getReference().child("invitations");
        databaseReferenceReport = firebaseDatabase.getReference().child("reports");
        databaseReferenceEvent = firebaseDatabase.getReference().child("events");
        databaseReferenceUserEvents = firebaseDatabase.getReference().child("userEvents");

    }

    public String insertGroup(Group g) {
        DatabaseReference newReference = databaseReferenceGroups.push();
        g.setId(newReference.getKey());

        newReference.setValue(g);
        return g.getId();
    }

    public void getGroupById(String groupId, ValueEventListener listener) {
        Query query = databaseReferenceGroups.orderByChild("id").equalTo(groupId);
        query.addListenerForSingleValueEvent(listener);
    }

    public void updateGroup(Group g) {
        databaseReferenceGroups.child(g.getId()).setValue(g);
    }

    public void deleteGroup(Group group) {
        databaseReferenceGroups.child(group.getId()).removeValue();

        databaseReferenceUserGroups.orderByChild("groupId").equalTo(group.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - GroupService - deleteGroup", error.getMessage());
            }
        });

        databaseReferenceMessage.orderByChild("groupId").equalTo(group.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - GroupService - deleteGroup", error.getMessage());
            }
        });

        databaseReferenceInvitation.orderByChild("groupId").equalTo(group.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - GroupService - deleteGroup", error.getMessage());
            }
        });

        databaseReferenceReport.orderByChild("groupId").equalTo(group.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Report report = snapshot.getValue(Report.class);
                        report.setGroupId(null);
                        databaseReferenceReport.child(report.getId()).setValue(report);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - GroupService - deleteGroup", error.getMessage());
            }
        });

        databaseReferenceEvent.orderByChild("groupId").equalTo(group.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Event> scheduledEvents = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Event event = snapshot.getValue(Event.class);
                        ZonedDateTime startDateTime = ZonedDateTime.parse(event.getStartDateAndTime(), DateTimeFormatter.ISO_DATE_TIME);
                        ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);

                        if(startDateTime.isAfter(today)){
                            scheduledEvents.add(event);
                        }else{
                            event.setGroupId(null);
                            databaseReferenceEvent.child(event.getId()).setValue(event);
                        }
                    }

                    for (Event event : scheduledEvents) {
                        databaseReferenceUserEvents.orderByChild("eventId").equalTo(event.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                databaseReferenceEvent.child(event.getId()).removeValue();
                                if(snapshot.exists()){
                                    for (DataSnapshot snapshotEvents : snapshot.getChildren()) {
                                        UserEvent userEvent = snapshotEvents.getValue(UserEvent.class);
                                        databaseReferenceUserEvents.child(userEvent.getId()).removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Error - GroupService - deleteGroup", error.getMessage());
                            }
                        });
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - GroupService - deleteGroup", error.getMessage());
            }
        });
    }

}