package com.example.eventjoy.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eventjoy.activities.MemberMainActivity;
import com.example.eventjoy.activities.PopupReauthenticateActivity;
import com.example.eventjoy.activities.SignUpActivity;
import com.example.eventjoy.callbacks.EventsCallback;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.callbacks.SimpleCallbackOnError;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.enums.Role;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EventService {

    private DatabaseReference databaseReferenceEvent;
    private DatabaseReference databaseReferenceUserEvent;

    private ValueEventListener eventsListener;
    private ValueEventListener userEventsListener;

    public EventService(Context context) {
        databaseReferenceEvent = FirebaseDatabase.getInstance().getReference().child("events");
        databaseReferenceUserEvent = FirebaseDatabase.getInstance().getReference().child("userEvents");
    }

    public void updateEvent(Event e) {
        databaseReferenceEvent.child(e.getId()).setValue(e);
    }

    public String insertEvent(Event e) {
        DatabaseReference newReference = databaseReferenceEvent.push();
        e.setId(newReference.getKey());

        newReference.setValue(e);
        return e.getId();
    }

    public void isUserRegisteredInOngoingEvent(String groupId, String userId, SimpleCallback callback) {
        databaseReferenceEvent.orderByChild("groupId").equalTo(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    eventList.add(event);
                }

                if (eventList.isEmpty()) {
                    callback.onSuccess("false");
                    return;
                }

                AtomicBoolean found = new AtomicBoolean(false);
                AtomicInteger processedCount = new AtomicInteger(0);
                int totalEvents = eventList.size();

                for (Event event : eventList) {
                    LocalDateTime startDateTimeEvent = LocalDateTime.parse(event.getStartDateAndTime());
                    LocalDateTime endDateTimeEvent = LocalDateTime.parse(event.getEndDateAndTime());
                    LocalDateTime today = LocalDateTime.now();
                    if (endDateTimeEvent.isAfter(today) && (startDateTimeEvent.isBefore(today) || startDateTimeEvent.equals(today))) {
                        databaseReferenceUserEvent.orderByChild("eventId").equalTo(event.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (found.get()) return;
                                    for (DataSnapshot snapshotUserEvent : snapshot.getChildren()) {
                                        UserEvent userEvent = snapshotUserEvent.getValue(UserEvent.class);
                                        if (userEvent.getUserId().equals(userId)) {
                                            found.set(true);
                                            callback.onSuccess("true");
                                        }
                                    }
                                    if (processedCount.incrementAndGet() == totalEvents && !found.get()) {
                                        callback.onSuccess("false");
                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onCancelled("Error querying database: " + error.getMessage());
                            }
                        });


                    } else {
                        if (processedCount.incrementAndGet() == totalEvents && !found.get()) {
                            callback.onSuccess("false");
                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - EventService - isUserRegisteredInOngoingEvent", error.getMessage());
                callback.onCancelled("Error querying the database: " + error.getMessage());
            }
        });

    }

    public void checkOngoingEvent(String groupId, SimpleCallback callback) {
        databaseReferenceEvent.orderByChild("groupId").equalTo(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //hacer esto en otro metodo
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Event event = snapshot.getValue(Event.class);
                        DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        LocalDateTime startDateTimeEvent = LocalDateTime.parse(event.getStartDateAndTime(), formatterDateTime);
                        LocalDateTime endDateTimeEvent = LocalDateTime.parse(event.getEndDateAndTime(), formatterDateTime);
                        String formattedToday = LocalDateTime.now().format(formatterDateTime);
                        LocalDateTime today = LocalDateTime.parse(formattedToday, formatterDateTime);
                        if (endDateTimeEvent.isAfter(today) && (startDateTimeEvent.isBefore(today) || startDateTimeEvent.equals(today))) {
                            callback.onSuccess("true");
                            return;
                        }
                    }
                    callback.onSuccess("false");
                    return;
                } else {
                    callback.onSuccess("false");
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - EventService - checkOngoingEvent", error.getMessage());
                callback.onCancelled("Error querying the database: " + error.getMessage());
            }
        });
    }

    public void getByMemberId(String memberId, EventsCallback callback) {
        if (userEventsListener != null) {
            databaseReferenceUserEvent.removeEventListener(userEventsListener);
        }

        userEventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> eventsId = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserEvent userEvent = snapshot.getValue(UserEvent.class);
                    eventsId.add(userEvent.getEventId());
                }

                if (eventsId.isEmpty()) {
                    Log.i("VACIO", eventsId.toString());
                    callback.onSuccess(new ArrayList<>());
                    return;
                }else{
                    Log.i("LLENO", eventsId.toString());
                }




                if (eventsListener != null) {
                    databaseReferenceEvent.removeEventListener(eventsListener);
                }

                eventsListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Event> events = new ArrayList<>();

                        for (DataSnapshot groupSnap : snapshot.getChildren()) {
                            Event event = groupSnap.getValue(Event.class);
                            if (event != null && eventsId.contains(event.getId())) {
                                events.add(event);
                            }
                        }
                        callback.onSuccess(events);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.toException());
                    }
                };
                databaseReferenceEvent.addValueEventListener(eventsListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceUserEvent.orderByChild("userId").equalTo(memberId).addValueEventListener(userEventsListener);
    }

    public void getByGroupId(String groupId, EventsCallback callback) {
        if (eventsListener != null) {
            databaseReferenceEvent.removeEventListener(eventsListener);
        }

        eventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    eventList.add(event);
                }

                if (eventList.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                } else {
                    callback.onSuccess(eventList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceEvent.orderByChild("groupId").equalTo(groupId).addValueEventListener(eventsListener);
    }

    public void checkOverlapingEvents(String memberId, Event eventCheck, SimpleCallbackOnError callback) {
        databaseReferenceUserEvent.orderByChild("userId").equalTo(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userEventIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserEvent userEvent = snapshot.getValue(UserEvent.class);
                    userEventIds.add(userEvent.getEventId());
                }

                if (userEventIds.isEmpty()) {
                    callback.onError(null);
                    return;
                }


                    databaseReferenceEvent.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Event event = snapshot.getValue(Event.class);
                                if(event.getGroupId()!=null && userEventIds.contains(event.getId())){

                                    OffsetDateTime offsetDateTime;

                                    offsetDateTime = OffsetDateTime.parse(event.getStartDateAndTime());
                                    LocalDateTime startAppointmentDB = offsetDateTime.toLocalDateTime();

                                    offsetDateTime = OffsetDateTime.parse(event.getEndDateAndTime());
                                    LocalDateTime endAppointmentDB = offsetDateTime.toLocalDateTime();

                                    offsetDateTime = OffsetDateTime.parse(eventCheck.getStartDateAndTime());
                                    LocalDateTime startAppointmentCheck = offsetDateTime.toLocalDateTime();

                                    offsetDateTime = OffsetDateTime.parse(eventCheck.getEndDateAndTime());
                                    LocalDateTime endAppointmentCheck = offsetDateTime.toLocalDateTime();


                                    if ((((startAppointmentCheck.isAfter(startAppointmentDB) || startAppointmentCheck.equals(startAppointmentDB)) &&
                                            (startAppointmentCheck.isBefore(endAppointmentDB))) || ((endAppointmentCheck.isAfter(startAppointmentDB)) &&
                                            (endAppointmentCheck.isBefore(endAppointmentDB) || endAppointmentCheck.equals(endAppointmentDB))) ||
                                            ((startAppointmentCheck.isBefore(startAppointmentDB) ||
                                                    startAppointmentCheck.equals(startAppointmentDB)) &&
                                                    (endAppointmentCheck.isAfter(endAppointmentDB) || endAppointmentCheck.equals(endAppointmentDB)))) &&
                                            !event.getId().equals(eventCheck.getId())) {
                                        callback.onError("This event overlaps with another event you have registered for");
                                        return;
                                    }
                                }
                            }
                            callback.onError(null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //En caso de error en la consulta, lo registramos en el log y notificamos mediante el callback
                            Log.e("Error - EventService - checkOverlapingEvents", error.getMessage());
                            callback.onCancelled("Error querying the database " + error.getMessage());
                        }
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //En caso de error en la consulta, lo registramos en el log y notificamos mediante el callback
                Log.e("Error - EventService - checkOverlapingEvents", error.getMessage());
                callback.onCancelled("Error querying the database " + error.getMessage());
            }
        });
    }

    public void stopListening() {
        if (eventsListener != null) {
            databaseReferenceEvent.removeEventListener(eventsListener);
            eventsListener = null;
        }
        if (userEventsListener != null) {
            databaseReferenceUserEvent.removeEventListener(userEventsListener);
            userEventsListener = null;
        }
    }
}
