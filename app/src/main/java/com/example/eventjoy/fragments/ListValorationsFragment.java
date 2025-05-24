package com.example.eventjoy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateValorationsActivity;
import com.example.eventjoy.activities.EditMemberActivity;
import com.example.eventjoy.activities.MemberMainActivity;
import com.example.eventjoy.activities.SignUpActivity;
import com.example.eventjoy.adapters.ValorationAdapter;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.enums.Role;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.ValorationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListValorationsFragment extends Fragment {

    private View rootView;
    private ListView lvValorations;
    private ValorationService valorationService;
    private List<Valoration> valorationList;
    private SharedPreferences sharedPreferences;
    private ValorationAdapter valorationAdapter;
    private String ratedUserId;
    private FloatingActionButton btnAddValoration;
    private Member m;
    private Boolean valorationMade;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_list_valoration, container, false);

        loadServices();
        loadComponents();

        btnAddValoration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valorationMade=false;
                for (Valoration valoration : valorationList) {
                    if(valoration.getRaterUserId().equals(sharedPreferences.getString("id", ""))){
                        Toast.makeText(getContext(), "You cannot rate the same member more than once.", Toast.LENGTH_SHORT).show();
                        valorationMade=true;
                        return;
                    }
                }

                if(!valorationMade){
                    Intent createValorationIntent = new Intent(getContext(), CreateValorationsActivity.class);
                    createValorationIntent.putExtra("member", m);
                    startActivity(createValorationIntent);
                }
            }
        });


        return  rootView;
    }

    private void loadComponents(){
        valorationList = new ArrayList<>();
        btnAddValoration = rootView.findViewById(R.id.btnAddValoration);
        lvValorations = rootView.findViewById(R.id.lvValorations);
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            m = (Member) getArguments().getSerializable("member");
            ratedUserId=m.getId();
        }else{
            ratedUserId = sharedPreferences.getString("id", "");
        }


        valorationService.getByRatedUserId(ratedUserId, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                valorationList.clear();

                for (DocumentSnapshot doc : snapshots) {
                    Valoration valoration = doc.toObject(Valoration.class);
                    valorationList.add(valoration);
                }

                valorationAdapter = new ValorationAdapter(getContext(), valorationList);
                lvValorations.setAdapter(valorationAdapter);
            }
        });
    }

    private void loadServices(){
        valorationService = new ValorationService(getContext());
    }

}