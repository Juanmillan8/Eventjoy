package com.example.eventjoy.callbacks;

import com.example.eventjoy.models.Group;

import java.util.List;

public interface GroupsCallback {
    void onSuccess(List<Group> groups);
    void onFailure(Exception e);
}
