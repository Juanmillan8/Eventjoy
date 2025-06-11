package com.example.eventjoy.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.callbacks.SimpleCallbackOnError;
import com.example.eventjoy.enums.EventStatus;
import com.example.eventjoy.models.Address;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
import com.example.eventjoy.services.EventService;
import com.example.eventjoy.services.UserEventService;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class CreateEventsActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private Button btnCreateEvent;
    private Group group;
    private Bundle getGroup;
    private ZonedDateTime eventStartDateTime, endDateTimeEvent;
    private DateTimeFormatter formatterDateTime;
    private EventService eventService;
    private TextInputEditText textInputEditTextStartDateAndTime, textInputEditTextTitle, textInputEditTextDescription,
            textInputEditTextDuration, textInputEditTextNumberOfParticipants, textInputEditTextStreet, textInputEditTextStreetNumber,
            textInputEditTextDoor, textInputEditTextFloor, textInputEditTextPostalCode, textInputEditTextCity, textInputEditTextProvince,
            textInputEditTextMunicipality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_events);
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

        btnCreateEvent.setOnClickListener(v -> {
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

            LocalDateTime date = LocalDateTime.parse(textInputEditTextStartDateAndTime.getText().toString().trim(), formatterDateTime);
            ZonedDateTime zonedDateTimeUtc = date.withSecond(0).withNano(0).atZone(ZoneOffset.UTC);
            eventStartDateTime = zonedDateTimeUtc;



            ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);
            String formattedEventStartDateTime = formatter.format(eventStartDateTime);
            if(eventStartDateTime.isBefore(today) || eventStartDateTime.equals(today)){
                Toast.makeText(getApplicationContext(), "The date must be after to today's date", Toast.LENGTH_SHORT).show();
            }else if (Integer.parseInt(textInputEditTextDuration.getText().toString())<=15){
                Toast.makeText(getApplicationContext(), "The duration of an event must be at least 15 minutes", Toast.LENGTH_SHORT).show();
            }else if (Integer.parseInt(textInputEditTextNumberOfParticipants.getText().toString())<=0){
                Toast.makeText(getApplicationContext(), "The maximum number of participants must be at least one", Toast.LENGTH_SHORT).show();
            }else{
                Event event = new Event();
                Address address = new Address();

                address.setCity(textInputEditTextCity.getText().toString());
                address.setDoor(textInputEditTextDoor.getText().toString());
                address.setFloor(textInputEditTextFloor.getText().toString());
                address.setMunicipality(textInputEditTextMunicipality.getText().toString());
                address.setStreet(textInputEditTextStreet.getText().toString());
                address.setNumberStreet(textInputEditTextStreetNumber.getText().toString());
                address.setPostalCode(textInputEditTextPostalCode.getText().toString());
                address.setProvince(textInputEditTextProvince.getText().toString());

                event.setAddress(address);
                event.setDescription(textInputEditTextDescription.getText().toString());
                event.setTitle(textInputEditTextTitle.getText().toString());
                event.setStatus(EventStatus.SCHEDULED);
                event.setMaxParticipants(Integer.parseInt(textInputEditTextNumberOfParticipants.getText().toString()));
                event.setStartDateAndTime(formattedEventStartDateTime);
                event.setGroupId(group.getId());


                endDateTimeEvent = eventStartDateTime.plusMinutes(
                        Integer.parseInt(textInputEditTextDuration.getText().toString())
                );

                event.setEndDateAndTime(formatter.format(endDateTimeEvent));
                eventService.insertEvent(event);
                Toast.makeText(getApplicationContext(), "Event successfully created", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents(){
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
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
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getGroup = getIntent().getExtras();
        group = (Group) getGroup.getSerializable("group");
        formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    }

    private void loadServices(){
        eventService = new EventService(getApplicationContext());
    }

}