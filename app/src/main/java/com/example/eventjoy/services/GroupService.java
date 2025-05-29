package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.models.UserGroup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupService {

    private DatabaseReference databaseReferenceGroups;
    private DatabaseReference databaseReferenceUserGroups;
    private DatabaseReference databaseReferenceMessage;
    private DatabaseReference databaseReferenceInvitation;
    private DatabaseReference databaseReferenceReport;
    private DatabaseReference databaseReferenceEvent;

    public GroupService(Context context) {
        databaseReferenceGroups = FirebaseDatabase.getInstance().getReference().child("groups");
        databaseReferenceUserGroups = FirebaseDatabase.getInstance().getReference().child("userGroups");
        databaseReferenceMessage = FirebaseDatabase.getInstance().getReference().child("messages");
        databaseReferenceInvitation = FirebaseDatabase.getInstance().getReference().child("invitations");
        databaseReferenceReport = FirebaseDatabase.getInstance().getReference().child("reports");
        databaseReferenceEvent = FirebaseDatabase.getInstance().getReference().child("events");
    }

    public String insertGroup(Group g) {
        DatabaseReference newReference = databaseReferenceGroups.push();
        g.setId(newReference.getKey());

        newReference.setValue(g);
        return g.getId();
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
                        Log.i("FORMESSAGES", "FORMESSAGES");
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
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Event event = snapshot.getValue(Event.class);
                        event.setGroupId(null);
                        databaseReferenceEvent.child(event.getId()).setValue(event);
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