package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventjoy.callbacks.InvitationsCallback;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Invitation;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Report;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InvitationService {

    private ValueEventListener invitationsListener;

    private DatabaseReference databaseReferenceInvitation;

    public InvitationService(Context context) {
        databaseReferenceInvitation = FirebaseDatabase.getInstance().getReference().child("invitations");
    }

    public void deleteInvitation(Invitation invitation) {
        databaseReferenceInvitation.child(invitation.getId()).removeValue();
    }

    public void getInvitations(String memberId, InvitationsCallback callback) {
        if (invitationsListener != null) {
            databaseReferenceInvitation.removeEventListener(invitationsListener);
        }

        invitationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Invitation> invitationList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Invitation invitation = snapshot.getValue(Invitation.class);
                    invitationList.add(invitation);
                }

                if (invitationList.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }else{
                    callback.onSuccess(invitationList);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceInvitation.orderByChild("invitedUserId").equalTo(memberId).addValueEventListener(invitationsListener);
    }

    public void hasAlreadyInvited(String memberId, String groupId, SimpleCallback callback) {
        databaseReferenceInvitation.orderByChild("invitedUserId").equalTo(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Invitation invitation = snapshot.getValue(Invitation.class);
                        if (invitation.getGroupId().equals(groupId)) {
                            callback.onSuccess("true");
                            return;
                        }
                    }
                    callback.onSuccess("false");
                } else {
                    callback.onSuccess("false");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - UserEventService - checkMemberIsParticipant", error.getMessage());
                callback.onCancelled("Error querying the database: " + error.getMessage());
            }
        });
    }

    public String insertInvitation(Invitation i) {
        DatabaseReference newReference = databaseReferenceInvitation.push();
        i.setId(newReference.getKey());

        newReference.setValue(i);
        return i.getId();
    }

    public void stopListening() {
        if (invitationsListener != null) {
            databaseReferenceInvitation.removeEventListener(invitationsListener);
            invitationsListener = null;
        }
    }

}
