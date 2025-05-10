package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventjoy.models.Member;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MemberService {

    private CollectionReference mFirestore;

    public MemberService(Context context) {
        mFirestore = FirebaseFirestore.getInstance().collection("members");
    }

    public void insertMember(Member m, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        String id = mFirestore.document().getId();
        m.setId(id);
        mFirestore.document(id).set(m).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                successListener.onSuccess(id);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failureListener.onFailure(e);
            }
        });
    }

    public void getMemberById(String memberId, OnSuccessListener<Member> successListener, OnFailureListener failureListener) {
        mFirestore.whereEqualTo("id", memberId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                Member member = doc.toObject(Member.class);
                successListener.onSuccess(member);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failureListener.onFailure(e);
            }
        });
    }

    public void getMemberByUid(String memberUid, OnSuccessListener<QuerySnapshot> successListener, OnFailureListener failureListener) {
        mFirestore
                .whereEqualTo("userAccountId", memberUid)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void checkRepeatedDNI(String patientDNI, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        mFirestore.whereEqualTo("dni", patientDNI).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Boolean exists = !queryDocumentSnapshots.isEmpty();
                successListener.onSuccess(exists.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failureListener.onFailure(e);
            }
        });
    }

    public void checkRepeatedUsername(String patientUsername, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        mFirestore.whereEqualTo("username", patientUsername).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Boolean exists = !queryDocumentSnapshots.isEmpty();
                successListener.onSuccess(exists.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failureListener.onFailure(e);
            }
        });
    }

    public void updateMember(Member m) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("dni", m.getDni());
        updates.put("phone", m.getPhone());
        updates.put("birthdate", m.getBirthdate());
        updates.put("username", m.getUsername());
        updates.put("level", m.getLevel());
        updates.put("provider", m.getProvider());
        updates.put("name", m.getName());
        updates.put("surname", m.getSurname());
        updates.put("photo", m.getPhoto());


        mFirestore.document(m.getId()).update(updates);
    }


}
