package com.example.eventjoy.callbacks;

import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Group;

import java.util.List;

public interface UserGroupRoleCallback {
    void onSuccess(UserGroupRole u);
    void onCancelled(String onCancelledMessage);
}
