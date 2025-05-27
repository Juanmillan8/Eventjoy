package com.example.eventjoy.services;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.eventjoy.models.Member;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MemberService {

    private DatabaseReference database;

    public MemberService(Context context) {
        database = FirebaseDatabase.getInstance().getReference().child("members");
    }

    public String insertMember(Member m) {
        DatabaseReference newReference = database.push();
        m.setId(newReference.getKey());

        newReference.setValue(m);
        return m.getId();
    }
    public void getMemberById(String memberId, ValueEventListener listener) {
        Query query = database.orderByChild("id").equalTo(memberId);
        query.addListenerForSingleValueEvent(listener);
    }

    public void getMemberByUid(String memberUid, ValueEventListener listener) {
        Query query = database.orderByChild("userAccountId").equalTo(memberUid);
        query.addListenerForSingleValueEvent(listener);
    }

    public void checkRepeatedDNI(String patientDNI, ValueEventListener listener) {
        Query query = database.orderByChild("dni").equalTo(patientDNI);
        query.addListenerForSingleValueEvent(listener);
    }

    public void checkRepeatedUsername(String patientUsername, ValueEventListener listener){
        Query query = database.orderByChild("username").equalTo(patientUsername);
        query.addListenerForSingleValueEvent(listener);
    }

    public void updateMember(Member m) {
        database.child(m.getId()).setValue(m);
    }


}
