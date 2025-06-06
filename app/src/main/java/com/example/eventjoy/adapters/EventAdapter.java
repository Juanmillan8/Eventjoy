package com.example.eventjoy.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.EventService;
import com.example.eventjoy.services.UserEventService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    private List<Event> events;
    private UserEventService userEventService;
    private Context context;

    //Constructor del adapter
    public EventAdapter(Context context, List<Event> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
        userEventService = new UserEventService(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Event event = this.events.get(position);
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        }
        TextView tvAddressEvent = convertView.findViewById(R.id.tvAddressEvent);
        TextView tvDateEvent = convertView.findViewById(R.id.tvDateEvent);
        TextView tvEventTime = convertView.findViewById(R.id.tvEventTime);
        TextView tvMembersEvent = convertView.findViewById(R.id.tvMembersEvent);
        TextView tvTitle = convertView.findViewById(R.id.tvTitleEvent);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        CardView cardViewStatus = convertView.findViewById(R.id.cardViewStatus);

        tvTitle.setText(event.getTitle());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        OffsetDateTime offsetStartDateTime = OffsetDateTime.parse(event.getStartDateAndTime());
        LocalDateTime startDateTime = offsetStartDateTime.toLocalDateTime();
        String startDateTimeString = startDateTime.format(formatter);

        OffsetDateTime offsetEndDateTime = OffsetDateTime.parse(event.getEndDateAndTime());
        LocalDateTime endDateTime = offsetEndDateTime.toLocalDateTime();
        String endDateTimeString = endDateTime.format(formatter);

        String[] partsDateTimeStart = startDateTimeString.split(" ");
        String dateStart = partsDateTimeStart[0];
        String timeStart = partsDateTimeStart[1];

        String[] partsDateTimeEnd = endDateTimeString.split(" ");
        String dateEnd = partsDateTimeEnd[0];
        String timeEnd= partsDateTimeEnd[1];

        tvDateEvent.setText(dateStart + " - " + dateEnd);
        tvEventTime.setText(timeStart + " - " + timeEnd);

            if(event.getStatus().name().equals("FINISHED")){
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.eventFinished));
            tvStatus.setText("Finished");
        }else if (event.getStatus().name().equals("ONGOING")){
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.eventOngoing));
            tvStatus.setText("Ongoing");
        }else{
            cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.eventScheduled));
            tvStatus.setText("Scheduled");
        }

        tvAddressEvent.setText("Street: " + event.getAddress().getStreet() + ", nº of the street: " + event.getAddress().getNumberStreet() + ", floor: " +
                event.getAddress().getFloor() + ", door: " + event.getAddress().getDoor() + ", postal code: " + event.getAddress().getPostalCode() + ", city: " +
                event.getAddress().getCity() + ", municipality: " + event.getAddress().getMunicipality() + ", province: " + event.getAddress().getProvince());

        userEventService.getByEventId(event.getId(), true, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserEvent> userEvents = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserEvent userEvent = snapshot.getValue(UserEvent.class);
                    userEvents.add(userEvent);
                }
                tvMembersEvent.setText(userEvents.size() + "/" + event.getMaxParticipants());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error querying database", databaseError.getMessage());
            }
        });



        return convertView;

    }

}