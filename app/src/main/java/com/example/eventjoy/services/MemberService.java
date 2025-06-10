package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberService {

    private DatabaseReference databaseReferenceMembers;
    private DatabaseReference databaseReferenceUserEvents;
    private DatabaseReference databaseReferenceUserGroups;

    private ValueEventListener userEventListener;
    private ValueEventListener memberListener;
    private ValueEventListener userGroupsListener;

    public MemberService(Context context) {
        databaseReferenceMembers = FirebaseDatabase.getInstance().getReference().child("members");
        databaseReferenceUserEvents = FirebaseDatabase.getInstance().getReference().child("userEvents");
        databaseReferenceUserGroups = FirebaseDatabase.getInstance().getReference().child("userGroups");
    }



    // Constructor para pruebas: recibe las referencias
    public MemberService(DatabaseReference membersRef, DatabaseReference userEventsRef, DatabaseReference userGroupsRef) {
        this.databaseReferenceMembers = membersRef;
        this.databaseReferenceUserEvents = userEventsRef;
        this.databaseReferenceUserGroups = userGroupsRef;
    }

    public String insertMember(Member m) {
        DatabaseReference newReference = databaseReferenceMembers.push();
        m.setId(newReference.getKey());

        newReference.setValue(m);
        return m.getId();
    }

    public void getMembersNotInGroup(String groupId, MembersCallback callback) {
        if (userGroupsListener != null) {
            databaseReferenceUserGroups.removeEventListener(userGroupsListener);
        }

        userGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserGroup userGroup = snapshot.getValue(UserGroup.class);
                    userIds.add(userGroup.getUserId());
                }

                if (memberListener != null) {
                    databaseReferenceMembers.removeEventListener(memberListener);
                }

                memberListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Member> members = new ArrayList<>();
                        for (DataSnapshot memberSnap : snapshot.getChildren()) {
                            Member member = memberSnap.getValue(Member.class);
                            if (!userIds.contains(member.getId())) {
                                members.add(member);
                            }
                        }
                        callback.onSuccess(members);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Error - MemberService - getMembersNotInGroup", error.getMessage());
                        callback.onFailure(error.toException());
                    }
                };
                databaseReferenceMembers.addValueEventListener(memberListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - MemberService - getMembersNotInGroup", error.getMessage());
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceUserGroups.orderByChild("groupId").equalTo(groupId).addValueEventListener(userGroupsListener);
    }

    public void getByEventId(String eventId, MembersCallback callback) {
        if (userEventListener != null) {
            databaseReferenceUserEvents.removeEventListener(userEventListener);
        }

        userEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserEvent userEvent = snapshot.getValue(UserEvent.class);
                    userIds.add(userEvent.getUserId());
                }

                if (userIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                if (memberListener != null) {
                    databaseReferenceMembers.removeEventListener(memberListener);
                }

                memberListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Member> members = new ArrayList<>();

                        for (DataSnapshot groupSnap : snapshot.getChildren()) {
                            Member member = groupSnap.getValue(Member.class);
                            if (member != null && userIds.contains(member.getId())) {
                                members.add(member);
                            }
                        }
                        callback.onSuccess(members);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.toException());
                    }
                };
                databaseReferenceMembers.addValueEventListener(memberListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceUserEvents.orderByChild("eventId").equalTo(eventId).addValueEventListener(userEventListener);
    }

    public void getMemberById(String memberId, ValueEventListener listener) {
        Query query = databaseReferenceMembers.orderByChild("id").equalTo(memberId);
        query.addListenerForSingleValueEvent(listener);
    }

    public void getMemberByUid(String memberUid, ValueEventListener listener) {
        Query query = databaseReferenceMembers.orderByChild("userAccountId").equalTo(memberUid);
        query.addListenerForSingleValueEvent(listener);
    }

    public void checkRepeatedDNI(String patientDNI, ValueEventListener listener) {
        Query query = databaseReferenceMembers.orderByChild("dni").equalTo(patientDNI);
        query.addListenerForSingleValueEvent(listener);
    }

    public void checkRepeatedUsername(String patientUsername, ValueEventListener listener) {
        Query query = databaseReferenceMembers.orderByChild("username").equalTo(patientUsername);
        query.addListenerForSingleValueEvent(listener);
    }

    public void deleteMemberById(String id) {
        databaseReferenceMembers.child(id).removeValue();
    }

    public void updateMember(Member m) {
        databaseReferenceMembers.child(m.getId()).setValue(m);
    }

    public void stopListening() {
        if (userEventListener != null) {
            databaseReferenceUserEvents.removeEventListener(userEventListener);
            userEventListener = null;
        }
        if (memberListener != null) {
            databaseReferenceMembers.removeEventListener(memberListener);
            memberListener = null;
        }
        if (userGroupsListener != null) {
            databaseReferenceUserGroups.removeEventListener(userGroupsListener);
            userGroupsListener = null;
        }
    }


}
