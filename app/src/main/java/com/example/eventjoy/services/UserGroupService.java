package com.example.eventjoy.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.UserGroup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserGroupService {
    private FirebaseFirestore mFirestore;

    public UserGroupService(Context context) {
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void insertUserGroup(UserGroup u, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        String id = mFirestore.collection("userGroups").document().getId();
        u.setId(id);
        mFirestore.collection("userGroups").document(id).set(u).addOnSuccessListener(new OnSuccessListener<Void>() {
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
    /*
    //Lista los grupos en los que esta registrado, todos y no ve cambios de modificacion
    public ListenerRegistration getGroups(String userId, GroupsCallback listener) {
        return mFirestore.collection("userGroups").whereEqualTo("userId", userId).addSnapshotListener((userGroupSnapshots, error) -> {
            if (error != null || userGroupSnapshots == null) {
                Log.w("Firestore", "Error escuchando userGroup", error);
                return;
            }

            List<String> groupIds = new ArrayList<>();
            for (DocumentSnapshot doc : userGroupSnapshots.getDocuments()) {
                String groupId = doc.getString("groupId");
                if (groupId != null) {
                    groupIds.add(groupId);
                }
            }

            if (groupIds.isEmpty()) {
                listener.onSuccess(new ArrayList<>());
                return;
            }

            mFirestore.collection("groups").whereIn(FieldPath.documentId(), groupIds).get().addOnSuccessListener(groupSnapshots -> {
                List<Group> resultGroups = new ArrayList<>();
                for (DocumentSnapshot groupDoc : groupSnapshots) {
                    Group group = groupDoc.toObject(Group.class);
                    if (group != null) {
                        resultGroups.add(group);
                    }
                }
                listener.onSuccess(resultGroups);
            }).addOnFailureListener(e -> Log.e("Firestore", "Error al obtener grupos", e));
        });
    }*/

    /*
    //Lista los grupos en los que esta registrado, ve modificacion pero se limita a 30 grupos
    public ListenerRegistration asdasasda(String userId, GroupsCallback callback) {
        return mFirestore.collection("userGroups").whereEqualTo("userId", userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot userGroupSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    callback.onFailure(e);
                    return;
                }

                if (userGroupSnapshots != null) {
                    List<String> groupIds = new ArrayList<>();
                    for (DocumentSnapshot doc : userGroupSnapshots.getDocuments()) {
                        String groupId = doc.getString("groupId");
                        if (groupId != null) {
                            groupIds.add(groupId);
                        }
                    }

                    if (groupIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    Log.d("GroupIDCount", "Total groupIds: " + groupIds.size());
                    mFirestore.collection("groups").whereIn(FieldPath.documentId(), groupIds).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot groupSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                callback.onFailure(e);
                                return;
                            }

                            if (groupSnapshots != null) {
                                Log.d("GroupSnapshotCount", "Groups recibidos: " + groupSnapshots.size());
                                List<Group> groups = new ArrayList<>();
                                for (DocumentSnapshot groupDoc : groupSnapshots.getDocuments()) {
                                    Group group = groupDoc.toObject(Group.class);
                                    group.setId(groupDoc.getId());
                                    groups.add(group);
                                }
                                callback.onSuccess(groups);
                            }
                        }
                    });
                }
            }
        });
    }*/

    public ListenerRegistration listenToUserGroups(String userId, GroupsCallback callback) {
        return mFirestore.collection("userGroups").whereEqualTo("userId", userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot userGroupSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    callback.onFailure(e);
                    return;
                }

                if (userGroupSnapshots.getMetadata().isFromCache()) {
                    Log.i("Snapshot", "Primera carga ignorada (desde caché)");
                    return;
                }

                Log.i("metodo", userGroupSnapshots.toString());
                if (userGroupSnapshots != null) {
                    List<String> groupIds = new ArrayList<>();
                    for (DocumentSnapshot doc : userGroupSnapshots.getDocuments()) {
                        String groupId = doc.getString("groupId");
                        if (groupId != null) {
                            groupIds.add(groupId);
                        }
                    }

                    if (groupIds.isEmpty()) {
                        Log.i("metodoEmpti", "metodoEmpti");
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }else{
                        Log.i("metodoNOEmpti", "metodoNOEmpti");
                    }

                    ArrayList<ArrayList<String>> listaDeListasDeGrupos = new ArrayList<>();

                    if (groupIds.size() > 30) {
                        while (!groupIds.isEmpty()) {
                            ArrayList<String> lote = new ArrayList<>();

                            int contador = Math.min(30, groupIds.size());

                            lote.addAll(groupIds.subList(0, contador)); // Agrega primeros 30 o menos
                            groupIds.subList(0, contador).clear(); // Elimina esos elementos de la lista original

                            listaDeListasDeGrupos.add(lote);
                        }
                    } else {
                        listaDeListasDeGrupos.add(new ArrayList<>(groupIds)); // Copia única si <= 30
                    }

                    Log.i("LISTADELISTAS", listaDeListasDeGrupos.toString());

                    AtomicInteger completed = new AtomicInteger(0);
                    int total = listaDeListasDeGrupos.size();
                    List<Group> groups = new ArrayList<>();
                    for (int i = 0; i < listaDeListasDeGrupos.size(); i++) {
                        mFirestore.collection("groups").whereIn(FieldPath.documentId(), listaDeListasDeGrupos.get(i)).addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot groupSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    callback.onFailure(e);
                                    return;
                                }

                                Log.i("OTROMETODO", "OTROMETODO");

                                if (groupSnapshots != null) {
                                    Log.i("GROUSNACHONONUL", "GROUSNACHONONUL");
                                    for (DocumentSnapshot groupDoc : groupSnapshots.getDocuments()) {
                                        Group group = groupDoc.toObject(Group.class);
                                        group.setId(groupDoc.getId());
                                        groups.add(group);
                                    }
                                    if (completed.incrementAndGet() == total) {
                                        Log.i("FIIIIIN", groups.toString());
                                        callback.onSuccess(groups);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    /*
    public ListenerRegistration getGroupsJuan(String userId, GroupsCallback listener) {
        return mFirestore.collection("userGroups").whereEqualTo("userId", userId).addSnapshotListener((userGroupSnapshots, error) -> {
            if (error != null || userGroupSnapshots == null) {
                Log.w("Firestore", "Error escuchando userGroup", error);
                return;
            }

            List<String> groupIds = new ArrayList<>();
            for (DocumentSnapshot doc : userGroupSnapshots.getDocuments()) {
                String groupId = doc.getString("groupId");
                if (groupId != null) {
                    groupIds.add(groupId);
                }
            }

            if (groupIds.isEmpty()) {
                listener.onSuccess(new ArrayList<>());
                return;
            }

            mFirestore.collection("groups").whereIn(FieldPath.documentId(), groupIds).get().addOnSuccessListener(groupSnapshots -> {
                List<Group> resultGroups = new ArrayList<>();
                for (DocumentSnapshot groupDoc : groupSnapshots) {
                    Group group = groupDoc.toObject(Group.class);
                    if (group != null) {
                        resultGroups.add(group);
                    }
                }
                listener.onSuccess(resultGroups);
            }).addOnFailureListener(e -> Log.e("Firestore", "Error al obtener grupos", e));
        });
    }*/



}
