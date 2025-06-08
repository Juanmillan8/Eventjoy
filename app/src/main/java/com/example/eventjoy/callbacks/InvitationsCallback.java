package com.example.eventjoy.callbacks;

import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Invitation;

import java.util.List;

public interface InvitationsCallback {
    void onSuccess(List<Invitation> invitations);
    void onFailure(Exception e);
}
