package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventjoy.R;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.adapters.MemberAdapter;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.callbacks.SimpleCallbackOnError;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.services.EventService;
import com.example.eventjoy.services.MemberService;
import com.example.eventjoy.services.UserEventService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {
    private Toolbar toolbarActivity;
    private Event event;
    private String role, idCurrentUser;
    private Bundle getData;
    private CardView cardViewStatus;
    private TextView tvStatus, tvDateEvent, tvEventTime, tvDescription, tvUbication, tvTitle, tvMembersNumber;
    private MemberService memberService;
    private UserEventService userEventService;
    private MemberAdapter memberAdapter;
    private RecyclerView lvMembers;
    private Button btnLeaveTheEvent, btnJoinTheEvent;
    private Boolean isParticipant;
    private ImageButton btnEditEvent, btnDeleteEvent;
    private EventService eventService;
    private DateTimeFormatter formatterDateTime;
    private LocalDateTime startDateTimeEvent;
    private LocalDateTime endDateTimeEvent;
    private SharedPreferences sharedPreferences;
    private Integer numParticipants;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

        btnEditEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZonedDateTime startDateTime = ZonedDateTime.parse(event.getStartDateAndTime(), DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);

                if (today.isBefore(startDateTime)) {
                    Intent editEventIntent = new Intent(getApplicationContext(), EditEventActivity.class);
                    editEventIntent.putExtra("event", event);
                    editEventIntent.putExtra("isParticipant", isParticipant);
                    editEventIntent.putExtra("role", role);
                    startActivity(editEventIntent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Only events that have not yet been started can be modified", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZonedDateTime startDateTime = ZonedDateTime.parse(event.getStartDateAndTime(), DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime endDateTime = ZonedDateTime.parse(event.getEndDateAndTime(), DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);

                if (endDateTime.isAfter(today) && (startDateTime.isBefore(today) || startDateTime.equals(today))) {
                    Toast.makeText(getApplicationContext(), "You cannot delete an event that is in progress", Toast.LENGTH_SHORT).show();
                } else {
                    if (startDateTime.isAfter(today)) {
                        userEventService.getByEventId(event.getId(), false, new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                eventService.deleteEvent(event);
                                if(dataSnapshot.exists()){
                                    for (DataSnapshot snapshotEvents : dataSnapshot.getChildren()) {
                                        UserEvent userEvent = snapshotEvents.getValue(UserEvent.class);
                                        userEventService.deleteUserEvent(userEvent);
                                    }
                                }
                                Toast.makeText(getApplicationContext(), "Event successfully deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Error - EventDetailsActivity - getByEventId", databaseError.getMessage());
                                Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        event.setGroupId(null);
                        eventService.updateEvent(event);
                        Toast.makeText(getApplicationContext(), "Event successfully deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });

        btnLeaveTheEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZonedDateTime startDateTime = ZonedDateTime.parse(event.getStartDateAndTime(), DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);

                if (startDateTime.isAfter(today)) {
                    userEventService.leaveEvent(event.getId(), idCurrentUser, new SimpleCallback() {
                        @Override
                        public void onSuccess(String message) {
                            if (message != null) {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                btnJoinTheEvent.setVisibility(View.VISIBLE);
                                btnLeaveTheEvent.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(String onCancelledMessage) {
                            Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "You can only exit unstarted events", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnJoinTheEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (numParticipants < event.getMaxParticipants()) {
                    eventService.checkOverlapingEvents(idCurrentUser, event, new SimpleCallbackOnError() {
                        @Override
                        public void onError(String errorMessage) {
                            if (errorMessage != null) {
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            } else {
                                UserEvent ue = new UserEvent();
                                ue.setEventId(event.getId());
                                ue.setUserId(idCurrentUser);
                                userEventService.joinTheEvent(ue);
                                Toast.makeText(getApplicationContext(), "You have successfully joined the event", Toast.LENGTH_SHORT).show();
                                btnJoinTheEvent.setVisibility(View.GONE);
                                btnLeaveTheEvent.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(String onCancelledMessage) {
                            Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "This event has no available places", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_details, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningMembers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        memberService.stopListening();
    }


    private void startListeningMembers() {
        memberService.stopListening();
        memberService.getByEventId(event.getId(), new MembersCallback() {
            @Override
            public void onSuccess(List<Member> members) {
                memberAdapter = new MemberAdapter(getApplicationContext(), members, false);
                lvMembers.setAdapter(memberAdapter);
                numParticipants = members.size();
                tvMembersNumber.setText(members.size() + "/" + event.getMaxParticipants() + " participants");
                if (members.size() == 0) {
                    lvMembers.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                    lvMembers.requestLayout();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error - EventDetailsActivity - getByEventId", e.getMessage());
                Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addReminder() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");

        String ubication = "Street: " + event.getAddress().getStreet() + ", nº of the street: " + event.getAddress().getNumberStreet() + ", floor: " + event.getAddress().getFloor() + ", door: " + event.getAddress().getDoor() + ", postal code: " + event.getAddress().getPostalCode() + ", city: " + event.getAddress().getCity() + ", municipality: " + event.getAddress().getMunicipality() + ", province: " + event.getAddress().getProvince();

        String localTimeStr;
        LocalDateTime localDateTime;
        ZonedDateTime zonedLocalTimeStart;
        ZonedDateTime zonedLocalTimeEnd;

        localTimeStr = event.getStartDateAndTime().replace("Z", "");
        localDateTime = LocalDateTime.parse(localTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        zonedLocalTimeStart = localDateTime.atZone(ZoneId.of("Europe/Madrid"));

        localTimeStr = event.getEndDateAndTime().replace("Z", "");
        localDateTime = LocalDateTime.parse(localTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        zonedLocalTimeEnd = localDateTime.atZone(ZoneId.of("Europe/Madrid"));

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, zonedLocalTimeStart.toInstant().toEpochMilli());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, zonedLocalTimeEnd.toInstant().toEpochMilli());
        intent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, ubication);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.dice_roller) {
            Intent diceRollerIntent = new Intent(getApplicationContext(), DiceRollerActivity.class);
            startActivity(diceRollerIntent);
        } else if (item.getItemId() == R.id.add_recordatory) {
            LocalDateTime today = LocalDateTime.now();
            if (startDateTimeEvent.isAfter(today)) {
                addReminder();
            } else if ((startDateTimeEvent.isBefore(today) || startDateTimeEvent.equals(today)) && endDateTimeEvent.isAfter(today)) {
                Toast.makeText(getApplicationContext(), "Can't add a reminder to an event that's already started", Toast.LENGTH_SHORT).show();
            } else if (endDateTimeEvent.isBefore(today) || endDateTimeEvent.equals(today)) {
                Toast.makeText(getApplicationContext(), "You cannot add a reminder for an event that has already ended", Toast.LENGTH_SHORT).show();
            }
        } else {
            finish();
        }
        return true;
    }

    private void loadComponents() {
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
        btnLeaveTheEvent = findViewById(R.id.btnLeaveTheEvent);
        btnJoinTheEvent = findViewById(R.id.btnJoinTheEvent);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        tvMembersNumber = findViewById(R.id.tvMembersNumber);
        lvMembers = findViewById(R.id.lvMembers);
        tvStatus = findViewById(R.id.tvStatus);
        tvDateEvent = findViewById(R.id.tvDateEvent);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvDescription = findViewById(R.id.tvDescription);
        tvUbication = findViewById(R.id.tvUbication);
        cardViewStatus = findViewById(R.id.cardViewStatus);
        tvTitle = findViewById(R.id.tvTitle);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lvMembers.setLayoutManager(new LinearLayoutManager(this));
        getData = getIntent().getExtras();
        event = (Event) getData.getSerializable("event");
        role = getData.getString("role");
        isParticipant = getData.getBoolean("isParticipant");

        formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        OffsetDateTime offsetStartDateTime = OffsetDateTime.parse(event.getStartDateAndTime());
        LocalDateTime startDateTime = offsetStartDateTime.toLocalDateTime();

        OffsetDateTime offsetEndDateTime = OffsetDateTime.parse(event.getEndDateAndTime());
        LocalDateTime endDateTime = offsetEndDateTime.toLocalDateTime();


        startDateTimeEvent = startDateTime;
        endDateTimeEvent = endDateTime;


        tvTitle.setText(event.getTitle());

        if (event.getStatus().name().equals("FINISHED")) {

        } else if (event.getStatus().name().equals("ONGOING")) {

        } else {

        }


        String startDateTimeString = startDateTime.format(formatterDateTime);
        String endDateTimeString = endDateTime.format(formatterDateTime);

        String[] partsDateTimeStart = startDateTimeString.split(" ");
        String dateStart = partsDateTimeStart[0];
        String timeStart = partsDateTimeStart[1];

        String[] partsDateTimeEnd = endDateTimeString.split(" ");
        String dateEnd = partsDateTimeEnd[0];
        String timeEnd = partsDateTimeEnd[1];

        tvDateEvent.setText(dateStart + " - " + dateEnd);
        tvEventTime.setText(timeStart + " - " + timeEnd);

        tvDescription.setText(event.getDescription());

        tvUbication.setText("Street: " + event.getAddress().getStreet() + ", nº of the street: " + event.getAddress().getNumberStreet() + ", floor: " + event.getAddress().getFloor() + ", door: " + event.getAddress().getDoor() + ", postal code: " + event.getAddress().getPostalCode() + ", city: " + event.getAddress().getCity() + ", municipality: " + event.getAddress().getMunicipality() + ", province: " + event.getAddress().getProvince());

        ZonedDateTime startEvent = ZonedDateTime.parse(event.getStartDateAndTime(), DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime endEvent = ZonedDateTime.parse(event.getEndDateAndTime(), DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);

        if(startEvent.isAfter(today)){
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eventScheduled));
            tvStatus.setText("Scheduled");
        }else if (endEvent.isAfter(today) && (startEvent.isBefore(today) || startDateTime.equals(today))) {
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eventOngoing));
            tvStatus.setText("Ongoing");
        }else if (endEvent.isBefore(today) || endEvent.equals(today)){
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eventFinished));
            tvStatus.setText("Finished");
        }

        if (role != null) {
            if (startEvent.isAfter(today)) {
                if (isParticipant) {
                    btnLeaveTheEvent.setVisibility(View.VISIBLE);
                } else {
                    btnJoinTheEvent.setVisibility(View.VISIBLE);
                }
                if (role.equals("ADMIN")) {
                    btnDeleteEvent.setVisibility(View.VISIBLE);
                    btnEditEvent.setVisibility(View.VISIBLE);
                }
            } else if (endEvent.isBefore(today) || endEvent.equals(today)) {
                if (role.equals("ADMIN")) {
                    btnDeleteEvent.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void loadServices() {
        memberService = new MemberService(getApplicationContext());
        eventService = new EventService(getApplicationContext());
        userEventService = new UserEventService(getApplicationContext());
    }

}