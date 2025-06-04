package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.adapters.MemberAdapter;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.EventService;
import com.example.eventjoy.services.MemberService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private Event event;
    private String role;
    private Bundle getData;
    private CardView cardViewStatus;
    private TextView tvStatus, tvDateEvent, tvEventTime, tvDescription, tvUbication, tvTitle, tvMembersNumber;
    private MemberService memberService;
    private MemberAdapter memberAdapter;
    private ListView lvMembers;
    private Button btnLeaveTheEvent, btnJoinTheEvent;
    private Boolean isParticipant;
    private ImageButton btnEditEvent, btnDeleteEvent;
    private EventService eventService;
    private DateTimeFormatter formatterDateTime;
    private LocalDateTime startDateTimeEvent;;
    private LocalDateTime endDateTimeEvent;

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
                String formattedToday = LocalDateTime.now().format(formatterDateTime);
                LocalDateTime today = LocalDateTime.parse(formattedToday, formatterDateTime);

                if(today.isBefore(startDateTimeEvent)){
                    Intent editEventIntent = new Intent(getApplicationContext(), EditEventActivity.class);
                    editEventIntent.putExtra("event", event);
                    editEventIntent.putExtra("isParticipant", isParticipant);
                    editEventIntent.putExtra("role", role);
                    startActivity(editEventIntent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Only events that have not yet been started can be modified", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOngoingEvent()){
                    Toast.makeText(getApplicationContext(), "You cannot delete an event that is in progress", Toast.LENGTH_SHORT).show();
                }else{
                    event.setGroupId(null);
                    eventService.updateEvent(event);
                    Toast.makeText(getApplicationContext(), "Event successfully deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private boolean isOngoingEvent(){
        String formattedToday = LocalDateTime.now().format(formatterDateTime);
        LocalDateTime today = LocalDateTime.parse(formattedToday, formatterDateTime);
        if (endDateTimeEvent.isAfter(today) && (startDateTimeEvent.isBefore(today) || startDateTimeEvent.equals(today))) {
            return true;
        }else{
            return false;
        }
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
                memberAdapter = new MemberAdapter(getApplicationContext(), members);
                lvMembers.setAdapter(memberAdapter);
                tvMembersNumber.setText(members.size() + "/" + event.getMaxParticipants() + " participants");
                if (members.size() == 0) {
                    lvMembers.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                    lvMembers.requestLayout();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents() {
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

        getData = getIntent().getExtras();
        event = (Event) getData.getSerializable("event");
        role = getData.getString("role");
        isParticipant = getData.getBoolean("isParticipant");

        formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        endDateTimeEvent = LocalDateTime.parse(event.getEndDateAndTime(), formatterDateTime);
        startDateTimeEvent = LocalDateTime.parse(event.getStartDateAndTime(), formatterDateTime);

        tvTitle.setText(event.getTitle());

        if (event.getStatus().name().equals("FINISHED")) {
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eventFinished));
            tvStatus.setText("Finished");
        } else if (event.getStatus().name().equals("ONGOING")) {
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eventOngoing));
            tvStatus.setText("Ongoing");
        } else {
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.eventScheduled));
            tvStatus.setText("Scheduled");
        }

        String[] partsDateTimeStart = event.getStartDateAndTime().split(" ");
        String dateStart = partsDateTimeStart[0];
        String timeStart = partsDateTimeStart[1];

        String[] partsDateTimeEnd = event.getEndDateAndTime().split(" ");
        String dateEnd = partsDateTimeEnd[0];
        String timeEnd = partsDateTimeEnd[1];

        tvDateEvent.setText(dateStart + " - " + dateEnd);
        tvEventTime.setText(timeStart + " - " + timeEnd);

        tvDescription.setText(event.getDescription());

        tvUbication.setText("Street: " + event.getAddress().getStreet() + ", nº of the street: " + event.getAddress().getNumberStreet() + ", floor: " + event.getAddress().getFloor() + ", door: " + event.getAddress().getDoor() + ", postal code: " + event.getAddress().getPostalCode() + ", city: " + event.getAddress().getCity() + ", municipality: " + event.getAddress().getMunicipality() + ", province: " + event.getAddress().getProvince());

        if(event.getStatus().name().equals("SCHEDULED")){
            if (isParticipant) {
                btnLeaveTheEvent.setVisibility(View.VISIBLE);
            }else{
                btnJoinTheEvent.setVisibility(View.VISIBLE);
            }
            if (role.equals("ADMIN")) {
                btnDeleteEvent.setVisibility(View.VISIBLE);
                btnEditEvent.setVisibility(View.VISIBLE);
            }
        }else if (event.getStatus().name().equals("FINISHED")){
            if (role.equals("ADMIN")) {
                btnDeleteEvent.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadServices() {
        memberService = new MemberService(getApplicationContext());
        eventService = new EventService(getApplicationContext());
    }

}