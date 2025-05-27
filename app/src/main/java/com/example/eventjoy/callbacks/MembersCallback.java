package com.example.eventjoy.callbacks;

import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;

import java.util.List;

public interface MembersCallback {
    void onSuccess(List<Member> members);
    void onFailure(Exception e);
}
