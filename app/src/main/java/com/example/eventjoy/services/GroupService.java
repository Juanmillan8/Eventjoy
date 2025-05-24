package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.models.Group;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupService {
    private FirebaseFirestore mFirestore;

    public GroupService(Context context) {
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void insertGroup(Group g, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        String id = mFirestore.collection("groups").document().getId();
        g.setId(id);
        mFirestore.collection("groups").document(id).set(g).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public ListenerRegistration listenToAllGroups(GroupsCallback callback) {
        return mFirestore.collection("groups").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    callback.onFailure(e);
                    return;
                }
                Log.i("METODOALGROUPS", "METODOALGROUPS");
                if (queryDocumentSnapshots != null) {
                    List<Group> groups = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Group group = doc.toObject(Group.class);
                        if (group != null) {
                            group.setId(doc.getId()); // Set ID manualmente si lo necesitas
                            groups.add(group);
                        }
                    }
                    Log.i("METODOALGROUPSLISTADO", groups.toString());
                    callback.onSuccess(groups);
                }
            }
        });
    }

}