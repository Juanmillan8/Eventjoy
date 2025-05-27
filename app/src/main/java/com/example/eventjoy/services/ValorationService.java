package com.example.eventjoy.services;
import android.content.Context;
import androidx.annotation.NonNull;
import com.example.eventjoy.models.Valoration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ValorationService {

    private DatabaseReference databaseReferenceValorations;

    public ValorationService(Context context) {
        databaseReferenceValorations = FirebaseDatabase.getInstance().getReference().child("valorations");
    }

    public String insertValoration(Valoration v) {
        DatabaseReference newReference = databaseReferenceValorations.push();
        v.setId(newReference.getKey());

        newReference.setValue(v);
        return v.getId();
    }

    public void getByRatedUserId(String ratedUserId, ValueEventListener listener) {
        Query query = databaseReferenceValorations.orderByChild("ratedUserId").equalTo(ratedUserId);
        query.addValueEventListener(listener);
    }
}