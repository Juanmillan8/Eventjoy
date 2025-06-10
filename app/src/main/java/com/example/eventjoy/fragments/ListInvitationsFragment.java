package com.example.eventjoy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.GroupActivity;
import com.example.eventjoy.activities.PopupJoinGroup;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.adapters.InvitationAdapter;
import com.example.eventjoy.adapters.MemberAdapter;
import com.example.eventjoy.callbacks.InvitationsCallback;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.UserGroupRoleCallback;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Invitation;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.GroupService;
import com.example.eventjoy.services.InvitationService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ListInvitationsFragment extends Fragment {

    private View rootView;
    private ListView lvInvitations;
    private List<Invitation> invitationList;
    private InvitationService invitationService;
    private InvitationAdapter invitationAdapter;
    private SharedPreferences sharedPreferences;
    private String idCurrentUser;
    private GroupService groupService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_list_invitations, container, false);

        loadServices();
        loadComponents();

        lvInvitations.setOnItemClickListener((parent, view, position, id) -> {
            Invitation invitation = (Invitation) parent.getItemAtPosition(position);
            groupService.getGroupById(invitation.getGroupId(), new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Group group = dataSnapshot.getChildren().iterator().next().getValue(Group.class);

                    Intent showPoPup = new Intent(getContext(), PopupJoinGroup.class);
                    showPoPup.putExtra("group", group);
                    showPoPup.putExtra("invitation", invitation);
                    startActivity(showPoPup);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Error - ListInvitationsFragment - getGroupById", databaseError.getMessage());
                    Toast.makeText(getContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return rootView;
    }

    private void startListeningInvitations() {
        invitationService.stopListening();
        invitationService.getInvitations(idCurrentUser, new InvitationsCallback() {
            @Override
            public void onSuccess(List<Invitation> invitations) {
                invitations.sort((i1, i2) -> {
                    ZonedDateTime date1 = ZonedDateTime.parse(i1.getInvitedAt());
                    ZonedDateTime date2 = ZonedDateTime.parse(i2.getInvitedAt());
                    return date2.compareTo(date1);
                });

                invitationList = new ArrayList<>();
                invitationList = invitations;
                invitationAdapter = new InvitationAdapter(getContext(), invitations);
                lvInvitations.setAdapter(invitationAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error querying database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningInvitations();
    }

    @Override
    public void onStop() {
        super.onStop();
        invitationService.stopListening();
    }

    private void loadComponents(){
        invitationList = new ArrayList<>();
        lvInvitations = rootView.findViewById(R.id.lvInvitations);
        invitationAdapter = new InvitationAdapter(getContext(), invitationList);
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
    }

    private void loadServices(){
        invitationService = new InvitationService(getContext());
        groupService = new GroupService(getContext());
    }

}