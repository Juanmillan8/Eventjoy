package com.example.eventjoy.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.enums.EventStatus;
import com.example.eventjoy.models.Address;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.services.EventService;
import com.example.eventjoy.services.UserEventService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditEventActivity extends AppCompatActivity {

    private EventService eventService;
    private Event eventEdit;
    private Toolbar toolbarActivity;
    private DateTimeFormatter formatterDateTime;
    private Button btnModifyEvent;
    private LocalDateTime eventStartDateTime;
    private UserEventService userEventService;
    private Bundle getData;
    private Boolean isParticipant;
    private String role;
    private TextInputEditText textInputEditTextStartDateAndTime, textInputEditTextTitle, textInputEditTextDescription,
            textInputEditTextDuration, textInputEditTextNumberOfParticipants, textInputEditTextStreet, textInputEditTextStreetNumber,
            textInputEditTextDoor, textInputEditTextFloor, textInputEditTextPostalCode, textInputEditTextCity, textInputEditTextProvince,
            textInputEditTextMunicipality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

        textInputEditTextStartDateAndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // Una vez seleccionada la fecha, se abre el TimePicker
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, selectedHour, selectedMinute) -> {
                    // Formateamos fecha y hora y lo ponemos en el EditText
                    String formattedDateTime = String.format(
                            "%02d/%02d/%04d %02d:%02d",
                            selectedDay, selectedMonth + 1, selectedYear,
                            selectedHour, selectedMinute
                    );
                    textInputEditTextStartDateAndTime.setText(formattedDateTime);
                }, hour, minute, true);

                timePickerDialog.show();

            }, year, month, day);

            datePickerDialog.show();
        });

        btnModifyEvent.setOnClickListener(v -> {
            verifications();
        });


    }

    private void verifications() {
        if (textInputEditTextStartDateAndTime.getText().toString().isBlank() || textInputEditTextTitle.getText().toString().isBlank() ||
                textInputEditTextDuration.getText().toString().isBlank() || textInputEditTextNumberOfParticipants.getText().toString().isBlank() ||
                textInputEditTextPostalCode.getText().toString().isBlank() || textInputEditTextStreet.getText().toString().isBlank() ||
                textInputEditTextStreetNumber.getText().toString().isBlank() || textInputEditTextCity.getText().toString().isBlank() ||
                textInputEditTextProvince.getText().toString().isBlank() || textInputEditTextMunicipality.getText().toString().isBlank()
        ) {
            Toast.makeText(getApplicationContext(), "You must fill out all the required fields", Toast.LENGTH_SHORT).show();
        } else {
            eventStartDateTime = LocalDateTime.parse(textInputEditTextStartDateAndTime.getText().toString().trim(), formatterDateTime);
            String formattedToday = LocalDateTime.now().format(formatterDateTime);
            LocalDateTime today = LocalDateTime.parse(formattedToday, formatterDateTime);

            if(eventStartDateTime.isBefore(today) || eventStartDateTime.equals(today)){
                Toast.makeText(getApplicationContext(), "The date must be after to today's date", Toast.LENGTH_SHORT).show();
            }else if (Integer.parseInt(textInputEditTextDuration.getText().toString())<=15){
                Toast.makeText(getApplicationContext(), "The duration of an event must be at least 15 minutes", Toast.LENGTH_SHORT).show();
            }else if (Integer.parseInt(textInputEditTextNumberOfParticipants.getText().toString())<=0){
                Toast.makeText(getApplicationContext(), "The maximum number of participants must be at least one", Toast.LENGTH_SHORT).show();
            }else{

                eventEdit.getAddress().setCity(textInputEditTextCity.getText().toString());
                eventEdit.getAddress().setDoor(textInputEditTextDoor.getText().toString());
                eventEdit.getAddress().setFloor(textInputEditTextFloor.getText().toString());
                eventEdit.getAddress().setMunicipality(textInputEditTextMunicipality.getText().toString());
                eventEdit.getAddress().setStreet(textInputEditTextStreet.getText().toString());
                eventEdit.getAddress().setNumberStreet(textInputEditTextStreetNumber.getText().toString());
                eventEdit.getAddress().setPostalCode(textInputEditTextPostalCode.getText().toString());
                eventEdit.getAddress().setProvince(textInputEditTextProvince.getText().toString());

                eventEdit.setDescription(textInputEditTextDescription.getText().toString());
                eventEdit.setTitle(textInputEditTextTitle.getText().toString());
                eventEdit.setStartDateAndTime(textInputEditTextStartDateAndTime.getText().toString());
                eventEdit.setMaxParticipants(Integer.parseInt(textInputEditTextNumberOfParticipants.getText().toString()));

                userEventService.getByEventId(eventEdit.getId(), false, new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(eventEdit.getMaxParticipants()<dataSnapshot.getChildrenCount()){
                            Toast.makeText(getApplicationContext(), "The maximum number of participants cannot be set lower than the number of people already registered for the event", Toast.LENGTH_SHORT).show();
                        }else{
                            LocalDateTime startDateTimeEvent = LocalDateTime.parse(textInputEditTextStartDateAndTime.getText().toString(), formatterDateTime);
                            LocalDateTime endDateTimeEvent = startDateTimeEvent.plusMinutes(
                                    Integer.parseInt(textInputEditTextDuration.getText().toString())
                            );

                        eventEdit.setEndDateAndTime(endDateTimeEvent.format(formatterDateTime));
                        eventService.updateEvent(eventEdit);
                        Toast.makeText(getApplicationContext(), "Event successfully modified", Toast.LENGTH_SHORT).show();
                            Intent detailsEventIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                            detailsEventIntent.putExtra("event", eventEdit);
                            detailsEventIntent.putExtra("isParticipant", isParticipant);
                            detailsEventIntent.putExtra("role", role);
                            startActivity(detailsEventIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Error querying database " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent detailsEventIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
        detailsEventIntent.putExtra("event", eventEdit);
        detailsEventIntent.putExtra("isParticipant", isParticipant);
        detailsEventIntent.putExtra("role", role);
        startActivity(detailsEventIntent);
        finish();
        return true;
    }

    private void loadComponents(){
        btnModifyEvent = findViewById(R.id.btnModifyEvent);
        formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textInputEditTextTitle = findViewById(R.id.textInputEditTextTitle);
        textInputEditTextDescription = findViewById(R.id.textInputEditTextDescription);
        textInputEditTextDuration = findViewById(R.id.textInputEditTextDuration);
        textInputEditTextNumberOfParticipants = findViewById(R.id.textInputEditTextNumberOfParticipants);
        textInputEditTextStreet = findViewById(R.id.textInputEditTextStreet);
        textInputEditTextStreetNumber = findViewById(R.id.textInputEditTextStreetNumber);
        textInputEditTextDoor = findViewById(R.id.textInputEditTextDoor);
        textInputEditTextFloor = findViewById(R.id.textInputEditTextFloor);
        textInputEditTextPostalCode = findViewById(R.id.textInputEditTextPostalCode);
        textInputEditTextCity = findViewById(R.id.textInputEditTextCity);
        textInputEditTextProvince = findViewById(R.id.textInputEditTextProvince);
        textInputEditTextMunicipality = findViewById(R.id.textInputEditTextMunicipality);
        textInputEditTextStartDateAndTime = findViewById(R.id.textInputEditTextStartDateAndTime);

        getData = getIntent().getExtras();
        eventEdit = (Event) getData.getSerializable("event");
        role = getData.getString("role");
        isParticipant = getData.getBoolean("isParticipant");

        textInputEditTextTitle.setText(eventEdit.getTitle());
        textInputEditTextDescription.setText(eventEdit.getDescription());
        textInputEditTextDuration.setText(String.valueOf(Duration.between(LocalDateTime.parse(eventEdit.getStartDateAndTime(), formatterDateTime), LocalDateTime.parse(eventEdit.getEndDateAndTime(), formatterDateTime)).toMinutes()));
        textInputEditTextNumberOfParticipants.setText(String.valueOf(eventEdit.getMaxParticipants()));
        textInputEditTextStreet.setText(eventEdit.getAddress().getStreet());
        textInputEditTextStreetNumber.setText(eventEdit.getAddress().getNumberStreet());
        textInputEditTextDoor.setText(eventEdit.getAddress().getDoor());
        textInputEditTextFloor.setText(eventEdit.getAddress().getFloor());
        textInputEditTextPostalCode.setText(eventEdit.getAddress().getPostalCode());
        textInputEditTextCity.setText(eventEdit.getAddress().getCity());
        textInputEditTextProvince.setText(eventEdit.getAddress().getProvince());
        textInputEditTextMunicipality.setText(eventEdit.getAddress().getMunicipality());
        textInputEditTextStartDateAndTime.setText(eventEdit.getStartDateAndTime());

    }

    private void loadServices(){
        eventService = new EventService(getApplicationContext());
        userEventService = new UserEventService(getApplicationContext());
    }

}