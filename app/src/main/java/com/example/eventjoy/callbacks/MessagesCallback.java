package com.example.eventjoy.callbacks;

import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Message;

import java.util.List;

public interface MessagesCallback {
    void onSuccess(List<Message> messages);
    void onFailure(Exception e);
}
