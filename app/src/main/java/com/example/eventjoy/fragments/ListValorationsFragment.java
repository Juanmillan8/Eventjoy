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
import android.widget.TextView;
import android.widget.Toast;
import com.example.eventjoy.R;
import com.example.eventjoy.activities.CreateValorationsActivity;
import com.example.eventjoy.adapters.MemberAdapter;
import com.example.eventjoy.adapters.ValorationAdapter;
import com.example.eventjoy.callbacks.ValorationsCallback;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.MemberService;
import com.example.eventjoy.services.ValorationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private MemberService memberService;
    private TextView tvUsername, tvAverageRatingsNumber, tvAverageRatingsText;

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
                Integer counter =0;
                Double acumulatorRatings = 0.0;
                Double averageRating = 0.0;
                for (Valoration valoration : valorations) {
                    acumulatorRatings += valoration.getRating();
                    counter++;
                }

                averageRating = acumulatorRatings/counter;

                if (Double.isNaN(averageRating)) {
                    tvAverageRatingsNumber.setText("0");
                }else{
                    tvAverageRatingsNumber.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
                }

                valorationList = valorations;
                valorationAdapter = new ValorationAdapter(getContext(), valorations);
                lvValorations.setAdapter(valorationAdapter);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity().getApplication(), "Error querying database", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadComponents(){
        tvAverageRatingsText = rootView.findViewById(R.id.tvAverageRatingsText);
        tvAverageRatingsNumber = rootView.findViewById(R.id.tvAverageRatingsNumber);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        valorationList = new ArrayList<>();
        btnAddValoration = rootView.findViewById(R.id.btnAddValoration);
        lvValorations = rootView.findViewById(R.id.lvValorations);
        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            m = (Member) getArguments().getSerializable("member");
            ratedUserId=m.getId();
            btnAddValoration.setVisibility(View.VISIBLE);
            tvUsername.setText(m.getUsername());
        }else{
            ratedUserId = sharedPreferences.getString("id", "");

            memberService.getMemberById(ratedUserId, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Member member = dataSnapshot.getChildren().iterator().next().getValue(Member.class);
                    tvUsername.setText(member.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Error - ListValorationsFragment - getMemberById", databaseError.getMessage());
                    Toast.makeText(getContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadServices(){
        valorationService = new ValorationService(getContext());
        memberService = new MemberService(getContext());
    }

}