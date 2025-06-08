package com.example.eventjoy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateEventsActivity;
import com.example.eventjoy.activities.DetailsGroupActivity;
import com.example.eventjoy.activities.EventDetailsActivity;
import com.example.eventjoy.activities.GroupActivity;
import com.example.eventjoy.activities.PopupJoinGroup;
import com.example.eventjoy.adapters.EventAdapter;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.callbacks.EventsCallback;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.callbacks.UserGroupRoleCallback;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.EventService;
import com.example.eventjoy.services.UserEventService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private View rootView;
    private String role;
    private Group group;
    private Member member;
    private FloatingActionButton btnCreateEvent;
    private ListView lvEvents;
    private EventService eventService;
    private List<Event> eventList;
    private EventAdapter eventAdapter;
    private UserEventService userEventService;
    private SharedPreferences sharedPreferences;
    private String idCurrentUser, idMemberUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_events, container, false);

        loadServices();
        loadComponents();

        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createEventsIntent = new Intent(getContext(), CreateEventsActivity.class);
                createEventsIntent.putExtra("group", group);
                startActivity(createEventsIntent);
            }
        });

        lvEvents.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);
            Intent detailsEventIntent = new Intent(getContext(), EventDetailsActivity.class);
            detailsEventIntent.putExtra("event", event);

            if (role != null) {
                detailsEventIntent.putExtra("role", role);
                userEventService.checkMemberIsParticipant(event.getId(), idCurrentUser, new SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (message.equals("Participant")) {
                            detailsEventIntent.putExtra("isParticipant", true);
                        } else {
                            detailsEventIntent.putExtra("isParticipant", false);
                        }
                        startActivity(detailsEventIntent);
                    }

                    @Override
                    public void onCancelled(String onCancelledMessage) {
                        Toast.makeText(getContext(), onCancelledMessage, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                if (idMemberUser == null) {
                    detailsEventIntent.putExtra("role", "PARTICIPANT");
                }
                detailsEventIntent.putExtra("isParticipant", true);
                startActivity(detailsEventIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        eventService.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (role != null) {
            startListeningEventsByGroupId();
        } else {
            startListeningEventsByMemberId();
        }
    }

    private void startListeningEventsByGroupId() {
        eventService.stopListening();
        eventService.getByGroupId(group.getId(), new EventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                events.sort((e1, e2) -> {
                    ZonedDateTime date1 = ZonedDateTime.parse(e1.getStartDateAndTime());
                    ZonedDateTime date2 = ZonedDateTime.parse(e2.getStartDateAndTime());
                    return date2.compareTo(date1);
                });

                eventList.clear();
                eventAdapter = new EventAdapter(getContext(), events);
                lvEvents.setAdapter(eventAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListeningEventsByMemberId() {
        eventService.stopListening();
        String idConsult = "";

        if (idMemberUser != null) {
            idConsult = idMemberUser;
        } else {
            idConsult = idCurrentUser;
        }

        eventService.getByMemberId(idConsult, new EventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                events.sort((e1, e2) -> {
                    ZonedDateTime date1 = ZonedDateTime.parse(e1.getStartDateAndTime());
                    ZonedDateTime date2 = ZonedDateTime.parse(e2.getStartDateAndTime());
                    return date2.compareTo(date1);
                });
                eventList.clear();
                eventAdapter = new EventAdapter(getContext(), events);
                lvEvents.setAdapter(eventAdapter);
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
        eventList = new ArrayList<>();
        lvEvents = rootView.findViewById(R.id.lvEvents);
        btnCreateEvent = rootView.findViewById(R.id.btnCreateEvent);
        if (getArguments() != null) {
            role = getArguments().getString("userGroupRole");
            group = (Group) getArguments().getSerializable("group");
            member = (Member) getArguments().getSerializable("member");
            if (member != null) {
                idMemberUser = member.getId();
            }
        }
        if (role != null && role.equals("ADMIN")) {
            btnCreateEvent.setVisibility(View.VISIBLE);
        }
    }

    private void loadServices() {
        userEventService = new UserEventService(getContext());
        eventService = new EventService(getContext());
    }

}