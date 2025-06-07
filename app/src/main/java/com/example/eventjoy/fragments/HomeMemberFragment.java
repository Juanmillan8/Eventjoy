package com.example.eventjoy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateGroupActivity;
import com.example.eventjoy.activities.GroupActivity;
import com.example.eventjoy.activities.PopupJoinGroup;
import com.example.eventjoy.activities.PopupReauthenticateActivity;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.adapters.ValorationAdapter;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.UserGroupRoleCallback;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.GroupService;
import com.example.eventjoy.services.UserGroupService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeMemberFragment extends Fragment implements SearchView.OnQueryTextListener{

    private View rootView;
    private FloatingActionButton btnAddGroup;
    private ListView lvGroups;
    private SearchView searchView;
    private UserGroupService userGroupService;
    private SharedPreferences sharedPreferences;
    private List<Group> groupList;
    private GroupAdapter groupAdapter;
    private String idCurrentUser;
    private ChipGroup chipGroupFilterList;
    private Chip chipMyGroups, chipOtherGroups, chipAllGroups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home_member, container, false);

        loadServices();
        loadComponents();

        btnAddGroup.setOnClickListener(v -> {
            Intent createGroupIntent = new Intent(getContext(), CreateGroupActivity.class);
            startActivity(createGroupIntent);
        });
        lvGroups.setOnItemClickListener((parent, view, position, id) -> {
            Group group = (Group) parent.getItemAtPosition(position);
            userGroupService.checkUserGroupRole(group.getId(), idCurrentUser, new UserGroupRoleCallback(){
                @Override
                public void onSuccess(UserGroupRole userGroupRole) {
                    if(userGroupRole.name().equals("ADMIN") || userGroupRole.name().equals("PARTICIPANT")){
                        Intent groupMainIntent = new Intent(getContext(), GroupActivity.class);
                        groupMainIntent.putExtra("group", group);
                        groupMainIntent.putExtra("userGroupRole", userGroupRole.name());
                        startActivity(groupMainIntent);
                    }else if(userGroupRole.name().equals("NO_PARTICIPANT")){
                        Intent showPoPup = new Intent(getContext(), PopupJoinGroup.class);
                        showPoPup.putExtra("group", group);
                        startActivity(showPoPup);
                    }
                }

                @Override
                public void onCancelled(String onCancelledMessage){
                    Toast.makeText(getContext(), onCancelledMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        chipMyGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListeningMyGroups();
            }
        });

        chipOtherGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListeningOtherGroups();
            }
        });

        chipAllGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListeningAllGroups();
            }
        });

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        userGroupService.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chipAllGroups.isChecked()) {
            startListeningAllGroups();
        } else if (chipOtherGroups.isChecked()) {
            startListeningOtherGroups();
        } else {
            chipMyGroups.setChecked(true);
            startListeningMyGroups();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home_member, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
    }

    private void startListeningMyGroups() {
        Log.i("migroups", "migroups");
        userGroupService.stopListening();
        userGroupService.getByMemberId(idCurrentUser, new GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                groupList.clear();
                groupAdapter = new GroupAdapter(getContext(), groups);
                lvGroups.setAdapter(groupAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListeningAllGroups() {
        userGroupService.stopListening();
        userGroupService.getAllGroups(idCurrentUser, new GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                groupList.clear();
                groupAdapter = new GroupAdapter(getContext(), groups);
                lvGroups.setAdapter(groupAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListeningOtherGroups() {
        userGroupService.stopListening();
        userGroupService.getOtherGroups(idCurrentUser, new GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                groupList.clear();
                groupAdapter = new GroupAdapter(getContext(), groups);
                lvGroups.setAdapter(groupAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComponents() {
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
        btnAddGroup = rootView.findViewById(R.id.btnAddGroup);
        chipMyGroups = rootView.findViewById(R.id.chipMyGroups);
        chipGroupFilterList = rootView.findViewById(R.id.chipGroupFilterList);
        chipOtherGroups = rootView.findViewById(R.id.chipOtherGroups);
        chipAllGroups = rootView.findViewById(R.id.chipAllGroups);
        groupList = new ArrayList<>();
        lvGroups = rootView.findViewById(R.id.lvGroups);
    }

    private void loadServices() {
        userGroupService = new UserGroupService(getContext());
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        groupAdapter.filter(newText);
        return false;
    }
}
