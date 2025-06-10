package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventjoy.callbacks.MessagesCallback;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private DatabaseReference databaseReferenceMessages;
    private ValueEventListener messageListener;

    public MessageService(Context context) {
        databaseReferenceMessages = FirebaseDatabase.getInstance().getReference().child("messages");
    }

    public MessageService(FirebaseDatabase firebaseDatabase) {
        databaseReferenceMessages = firebaseDatabase.getReference().child("messages");
    }

    public String insertMessage(Message m) {
        DatabaseReference newReference = databaseReferenceMessages.push();
        m.setId(newReference.getKey());

        newReference.setValue(m);
        return m.getId();
    }

    public void getByGroupId(String groupId, MessagesCallback callback){
        if (messageListener != null) {
            databaseReferenceMessages.removeEventListener(messageListener);
        }

        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }

                if (messages.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                }else{
                    callback.onSuccess(messages);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - MessageService - getByGroupId", error.getMessage());
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceMessages.orderByChild("groupId").equalTo(groupId).addValueEventListener(messageListener);
    }

    public void stopListening() {
        if (messageListener != null) {
            databaseReferenceMessages.removeEventListener(messageListener);
            messageListener = null;
        }
    }


}
