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
import com.example.eventjoy.adapters.MemberAdapter;
import com.example.eventjoy.adapters.ValorationAdapter;
import com.example.eventjoy.callbacks.ValorationsCallback;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.ValorationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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
                        Toast.makeText(getContext(), "You cannot rate the same member more than once", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onStart() {
        super.onStart();
        startListeningValorations();
    }

    @Override
    public void onStop() {
        super.onStop();
        valorationService.stopListening();
    }

    private void startListeningValorations() {
        valorationService.getByRatedUserId(ratedUserId, new ValorationsCallback() {
            @Override
            public void onSuccess(List<Valoration> valorations) {
                valorationList = valorations;
                valorationAdapter = new ValorationAdapter(getContext(), valorations);
                lvValorations.setAdapter(valorationAdapter);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity().getApplication(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadComponents(){
        valorationList = new ArrayList<>();
        btnAddValoration = rootView.findViewById(R.id.btnAddValoration);
        lvValorations = rootView.findViewById(R.id.lvValorations);
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            m = (Member) getArguments().getSerializable("member");
            ratedUserId=m.getId();
            btnAddValoration.setVisibility(View.VISIBLE);
        }else{
            ratedUserId = sharedPreferences.getString("id", "");
        }
    }

    private void loadServices(){
        valorationService = new ValorationService(getContext());
    }

}