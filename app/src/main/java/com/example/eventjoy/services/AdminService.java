package com.example.eventjoy.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.models.Admin;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserEvent;
import com.example.eventjoy.models.UserGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private DatabaseReference databaseReferenceAdmins;

    public AdminService(Context context) {
        databaseReferenceAdmins = FirebaseDatabase.getInstance().getReference().child("admins");
    }

    public AdminService(FirebaseDatabase firebaseDatabase) {
        databaseReferenceAdmins = firebaseDatabase.getReference().child("admins");
    }

    public void getAdminById(String adminId, ValueEventListener listener) {
        Query query = databaseReferenceAdmins.orderByChild("id").equalTo(adminId);
        query.addListenerForSingleValueEvent(listener);
    }

    public void getAdminByUid(String adminUid, ValueEventListener listener) {
        Query query = databaseReferenceAdmins.orderByChild("userAccountId").equalTo(adminUid);
        query.addListenerForSingleValueEvent(listener);
    }

    public void updateAdmin(Admin a) {
        databaseReferenceAdmins.child(a.getId()).setValue(a);
    }
}
