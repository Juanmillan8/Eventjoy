package com.example.eventjoy.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eventjoy.activities.MemberMainActivity;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.callbacks.UserGroupRoleCallback;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.enums.Visibility;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserGroup;
import com.example.eventjoy.models.Valoration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class UserGroupService {

    private DatabaseReference databaseReferenceUserGroups;
    private DatabaseReference databaseReferenceGroups;
    private DatabaseReference databaseReferenceMembers;

    private ValueEventListener userGroupsListener;
    private ValueEventListener groupsListener;
    private ValueEventListener memberListener;

    public UserGroupService(Context context) {
        databaseReferenceUserGroups = FirebaseDatabase.getInstance().getReference().child("userGroups");
        databaseReferenceGroups = FirebaseDatabase.getInstance().getReference().child("groups");
        databaseReferenceMembers = FirebaseDatabase.getInstance().getReference().child("members");
    }

    public String insertUserGroup(UserGroup u) {
        DatabaseReference newReference = databaseReferenceUserGroups.push();
        u.setId(newReference.getKey());

        newReference.setValue(u);
        return u.getId();
    }

    public void deleteUserGroup(String groupId, String userId, SimpleCallback callback) {
        databaseReferenceUserGroups.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("GROUP ID", groupId);
                UserGroup userGroup = new UserGroup();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (userGroup.getGroupId().equals(groupId)) {
                        userGroup = snapshot.getValue(UserGroup.class);
                        Log.i("US" + userGroup.getGroupId(), groupId);
                        break;
                    }

                }
                Log.i("PRIMERO", "SESALE PRIMERO");
                Log.i("USERGROUP", userGroup.getId());
                databaseReferenceUserGroups.child(userGroup.getId()).removeValue();
                Log.i("PRIMERO2", "PRIMERO2");
                callback.onSuccess("You have successfully left the group");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - UserGroupService - deleteUserGroup", error.getMessage());
                callback.onCancelled("Error querying the database: " + error.getMessage());
            }
        });
    }

    public void checkUserGroupRole(String groupId, String userId, UserGroupRoleCallback callback) {
        databaseReferenceUserGroups.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserGroup userGroup = snapshot.getValue(UserGroup.class);
                        Log.i("USERGROUP", userGroup.getId());
                        Log.i("ISADMIN", userGroup.getAdmin().toString());
                        if (userGroup.getGroupId().equals(groupId)) {
                            if (userGroup.getAdmin()) {
                                callback.onSuccess(UserGroupRole.ADMIN);
                                return;
                            } else {
                                callback.onSuccess(UserGroupRole.PARTICIPANT);
                                return;
                            }
                        }
                    }
                    callback.onSuccess(UserGroupRole.NO_PARTICIPANT);
                } else {
                    callback.onSuccess(UserGroupRole.NO_PARTICIPANT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - TimetableService - checkOverlapingTimetables", error.getMessage());
                callback.onCancelled("Error querying the database: " + error.getMessage());
            }
        });
    }

    public void getMembersByGroupId(String groupId, String userId, MembersCallback callback) {
        if (userGroupsListener != null) {
            databaseReferenceUserGroups.removeEventListener(userGroupsListener);
        }

        userGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserGroup userGroup = snapshot.getValue(UserGroup.class);
                    if (!userGroup.getUserId().equals(userId)) {
                        userIds.add(userGroup.getUserId());
                    }
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
                            if (userIds.contains(member.getId())) {
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
        databaseReferenceUserGroups.orderByChild("groupId").equalTo(groupId).addValueEventListener(userGroupsListener);
    }

    public void getOtherGroups(String userId, GroupsCallback callback) {
        if (userGroupsListener != null) {
            databaseReferenceUserGroups.removeEventListener(userGroupsListener);
        }

        userGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userGroupIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserGroup userGroup = snapshot.getValue(UserGroup.class);
                    userGroupIds.add(userGroup.getGroupId());
                }

                if (groupsListener != null) {
                    databaseReferenceGroups.removeEventListener(groupsListener);
                }

                groupsListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Group> groups = new ArrayList<>();

                        for (DataSnapshot groupSnap : snapshot.getChildren()) {
                            Group group = groupSnap.getValue(Group.class);
                            if (!group.getVisibility().equals(Visibility.PRIVATE)) {
                                if (!userGroupIds.contains(group.getId())) {
                                    groups.add(group);
                                }
                            }
                        }
                        callback.onSuccess(groups);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.toException());
                    }
                };
                databaseReferenceGroups.addValueEventListener(groupsListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceUserGroups.orderByChild("userId").equalTo(userId).addValueEventListener(userGroupsListener);
    }


    public void getAllGroups(String userId, GroupsCallback callback) {
        if (userGroupsListener != null) {
            databaseReferenceUserGroups.removeEventListener(userGroupsListener);
        }

        userGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userGroupIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserGroup userGroup = snapshot.getValue(UserGroup.class);
                    userGroupIds.add(userGroup.getGroupId());
                }

                if (groupsListener != null) {
                    databaseReferenceGroups.removeEventListener(groupsListener);
                }

                groupsListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Group> groups = new ArrayList<>();

                        for (DataSnapshot groupSnap : snapshot.getChildren()) {
                            Group group = groupSnap.getValue(Group.class);
                            if (group.getVisibility().equals(Visibility.PRIVATE)) {
                                if (userGroupIds.contains(group.getId())) {
                                    groups.add(group);
                                }
                            } else {
                                groups.add(group);
                            }


                        }
                        callback.onSuccess(groups);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.toException());
                    }
                };
                databaseReferenceGroups.addValueEventListener(groupsListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceUserGroups.orderByChild("userId").equalTo(userId).addValueEventListener(userGroupsListener);
    }

    public void getByMemberId(String userId, GroupsCallback callback) {
        if (userGroupsListener != null) {
            databaseReferenceUserGroups.removeEventListener(userGroupsListener);
        }

        userGroupsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userGroupIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserGroup userGroup = snapshot.getValue(UserGroup.class);
                    userGroupIds.add(userGroup.getGroupId());
                }

                if (userGroupIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                if (groupsListener != null) {
                    databaseReferenceGroups.removeEventListener(groupsListener);
                }

                groupsListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Group> groups = new ArrayList<>();

                        for (DataSnapshot groupSnap : snapshot.getChildren()) {
                            Group group = groupSnap.getValue(Group.class);
                            if (group != null && userGroupIds.contains(group.getId())) {
                                groups.add(group);
                            }
                        }
                        callback.onSuccess(groups);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.toException());
                    }
                };
                databaseReferenceGroups.addValueEventListener(groupsListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceUserGroups.orderByChild("userId").equalTo(userId).addValueEventListener(userGroupsListener);
    }

    public void stopListening() {
        if (userGroupsListener != null) {
            databaseReferenceUserGroups.removeEventListener(userGroupsListener);
            userGroupsListener = null;
        }
        if (groupsListener != null) {
            databaseReferenceGroups.removeEventListener(groupsListener);
            groupsListener = null;
        }
        if (memberListener != null) {
            databaseReferenceMembers.removeEventListener(memberListener);
            memberListener = null;
        }
    }

}
