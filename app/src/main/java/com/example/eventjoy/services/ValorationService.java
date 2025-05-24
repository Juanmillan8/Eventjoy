package com.example.eventjoy.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ValorationService {

    private CollectionReference mFirestore;

    public ValorationService(Context context) {
        mFirestore = FirebaseFirestore.getInstance().collection("valorations");
    }

    public void insertValoration(Valoration v, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        String id = mFirestore.document().getId();
        v.setId(id);
        mFirestore.document(id).set(v).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void getByRatedUserId(String ratedUserId, EventListener<QuerySnapshot> eventListener) {
        mFirestore
                .whereEqualTo("ratedUserId", ratedUserId)
                .addSnapshotListener(eventListener);
    }
}