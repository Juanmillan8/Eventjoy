package com.example.eventjoy.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.models.Event;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Message;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.services.GroupService;
import com.example.eventjoy.services.MemberService;
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

public class MessageAdapter extends ArrayAdapter<Message> {

    private List<Message> messages;
    private MemberService memberService;
    private Context context;

    //Constructor del adapter
    public MessageAdapter(Context context, List<Message> messages){
        super(context, 0, messages);
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Message message = this.messages.get(position);
        memberService = new MemberService(context);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
        }

        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvContent = convertView.findViewById(R.id.tvContent);
        ImageView profileIcon = convertView.findViewById(R.id.profileIcon);
        tvContent.setText(message.getContent());

        memberService.getMemberById(message.getSenderUserId(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Member member = dataSnapshot.getChildren().iterator().next().getValue(Member.class);

                if (member.getPhoto() != null && !member.getPhoto().isEmpty()) {
                    Picasso.get().load(member.getPhoto()).placeholder(R.drawable.default_profile_photo).into(profileIcon);
                }

                tvUsername.setText(member.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error - ValorationAdapter - getMemberById", databaseError.getMessage());
            }
        });

        return convertView;

    }

}
