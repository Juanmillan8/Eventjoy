package com.example.eventjoy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateValorationsActivity;
import com.example.eventjoy.activities.PopupJoinGroup;
import com.example.eventjoy.adapters.InvitationAdapter;
import com.example.eventjoy.adapters.MessageAdapter;
import com.example.eventjoy.callbacks.InvitationsCallback;
import com.example.eventjoy.callbacks.MessagesCallback;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Invitation;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Message;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.MessageService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    private View rootView;
    private ListView lvMessages;
    private MessageService messageService;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private Group group;
    private FloatingActionButton btnSendMessage;
    private TextInputEditText textInputEditTextMessage;
    private SharedPreferences sharedPreferences;
    private String idCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        loadServices();
        loadComponents();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!textInputEditTextMessage.getText().toString().isBlank()){
                    sendMessage();
                }
            }
        });

        return rootView;
    }

    private void sendMessage(){
        Message message = new Message();
        message.setContent(textInputEditTextMessage.getText().toString());
        message.setGroupId(group.getId());
        message.setSenderUserId(idCurrentUser);

        LocalDateTime localDateTime = LocalDateTime.now().withNano(0);
        ZonedDateTime utcDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        String formattedToday = utcDateTime.format(DateTimeFormatter.ISO_INSTANT);

        message.setSentAt(formattedToday);

        messageService.insertMessage(message);
        textInputEditTextMessage.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningMessages();
    }

    @Override
    public void onStop() {
        super.onStop();
        messageService.stopListening();
    }

    private void startListeningMessages() {
        messageService.stopListening();
        messageService.getByGroupId(group.getId(), new MessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                messages.sort((m1, m2) -> {
                    ZonedDateTime date1 = ZonedDateTime.parse(m1.getSentAt());
                    ZonedDateTime date2 = ZonedDateTime.parse(m2.getSentAt());
                    return date1.compareTo(date2);
                });
                messageList = new ArrayList<>();
                messageList = messages;
                messageAdapter = new MessageAdapter(getContext(), messages);
                lvMessages.setAdapter(messageAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadComponents(){
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
        textInputEditTextMessage = rootView.findViewById(R.id.textInputEditTextMessage);
        btnSendMessage = rootView.findViewById(R.id.btnSendMessage);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(getContext(), messageList);
        messageList = new ArrayList<>();
        lvMessages = rootView.findViewById(R.id.lvMessages);

        if (getArguments() != null) {
            group = (Group) getArguments().getSerializable("group");
        }
    }

    private void loadServices(){
        messageService = new MessageService(getContext());
    }

}