package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.ValorationsCallback;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserGroup;
import com.example.eventjoy.models.Valoration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ValorationService {

    private DatabaseReference databaseReferenceValorations;
    private ValueEventListener valorationsListener;

    public ValorationService(Context context) {
        databaseReferenceValorations = FirebaseDatabase.getInstance().getReference().child("valorations");
    }

    public String insertValoration(Valoration v) {
        DatabaseReference newReference = databaseReferenceValorations.push();
        v.setId(newReference.getKey());

        newReference.setValue(v);
        return v.getId();
    }


    public void getByRatedUserId(String ratedUserId, ValorationsCallback callback) {
        if (valorationsListener != null) {
            databaseReferenceValorations.removeEventListener(valorationsListener);
        }
        valorationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Valoration> valorations = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Valoration valoration = snapshot.getValue(Valoration.class);
                    valorations.add(valoration);
                }
                callback.onSuccess(valorations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - ValorationService - getByRatedUserId", error.getMessage());
                callback.onFailure(error.toException());
            }
        };
        databaseReferenceValorations.orderByChild("ratedUserId").equalTo(ratedUserId).addValueEventListener(valorationsListener);
    }

    public void stopListening() {
        if (valorationsListener != null) {
            databaseReferenceValorations.removeEventListener(valorationsListener);
            valorationsListener = null;
        }
    }

}