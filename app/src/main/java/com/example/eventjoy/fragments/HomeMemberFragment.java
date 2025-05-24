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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateGroupActivity;
import com.example.eventjoy.activities.GroupActivity;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.services.GroupService;
import com.example.eventjoy.services.UserGroupService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeMemberFragment extends Fragment {

    private View rootView;
    private FloatingActionButton btnAddGroup;
    private ListView lvGroups;
    private SearchView searchView;
    private UserGroupService userGroupService;
    private GroupService groupService;
    private SharedPreferences sharedPreferences;
    private List<Group> groupList;
    private GroupAdapter groupAdapter;
    private ListenerRegistration listener;
    private final Map<String, ListenerRegistration> groupListeners = new HashMap<>();
    private final Map<String, Group> groupCache = new HashMap<>();
    private String idCurrentUser;
    private ChipGroup chipGroupFilterList;
    private Chip chipMyGroups, chipOtherGroups, chipAllGroups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home_member, container, false);

        loadServices();
        loadComponents();

        btnAddGroup.setOnClickListener(v -> {
            Intent createGroupIntent = new Intent(getContext(), CreateGroupActivity.class);
            startActivity(createGroupIntent);
        });

        lvGroups.setOnItemClickListener((parent, view, position, id) -> {
            Group group = (Group) parent.getItemAtPosition(position);
            Intent groupMainIntent = new Intent(getContext(), GroupActivity.class);
            groupMainIntent.putExtra("group", group);
            startActivity(groupMainIntent);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home_member, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
        startListeningMyGroups();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("unono", "unono");
        removeAllListeners();
    }

    private void startListeningMyGroups() {
        removeAllListeners();
        Log.i("4", "4");
        Log.i("IDCURRENTUSER", idCurrentUser);
        listener = userGroupService.listenToUserGroups(idCurrentUser, new GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                Log.i("5", "5");
                requireActivity().runOnUiThread(() -> {
                    Log.i("ONSUCCEEESSS1", "ONSUCCEEESSSS1");
                    Log.i("GGGGGRRRRRROOOUUUUPPPSSSSS1", groups.toString());
                    groupList.clear();
                    groupList.addAll(groups);
                    groupAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "Error escuchando grupos", e);
            }
        });
    }

    private void startListeningAllGroups() {
        removeAllListeners();
        Log.i("ALGROUPS4", "ALGROUPS4");
        listener = groupService.listenToAllGroups(new GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                Log.i("ALGROUPS5", "ALGROUPS5");
                requireActivity().runOnUiThread(() -> {
                    Log.i("ONSUCCEEESSS2", "ONSUCCEEESSSS2");
                    Log.i("GGGGGRRRRRROOOUUUUPPPSSSSS2", groups.toString());
                    groupList.clear();
                    groupList.addAll(groups);
                    groupAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "Error escuchando grupos", e);
            }
        });

    }

    private void removeAllListeners() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
        Log.i("2", "2");
        for (ListenerRegistration reg : groupListeners.values()) {
            reg.remove();
        }
        Log.i("3", "3");
        groupListeners.clear();
        groupCache.clear();
    }

    private void loadComponents() {
        lvGroups = rootView.findViewById(R.id.lvGroups);
        btnAddGroup = rootView.findViewById(R.id.btnAddGroup);
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(getActivity(), groupList);
        lvGroups.setAdapter(groupAdapter);
        chipMyGroups = rootView.findViewById(R.id.chipMyGroups);
        chipGroupFilterList = rootView.findViewById(R.id.chipGroupFilterList);
        chipOtherGroups = rootView.findViewById(R.id.chipOtherGroups);
        chipAllGroups = rootView.findViewById(R.id.chipAllGroups);
    }

    private void loadServices() {
        userGroupService = new UserGroupService(getContext());
        groupService = new GroupService(getContext());
    }
}
