package com.example.eventjoy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eventjoy.R;
import com.example.eventjoy.activities.PopupEditAccountActivity;
import com.example.eventjoy.activities.SignUpActivity;
import com.example.eventjoy.models.Member;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DetailsMemberFragment extends Fragment {

    private View rootView;
    private TextView tvNameAndSurname, tvUsername, tvPhoneNumber, tvDni, tvBirthdate, tvEmail, tvLevel;
    private SharedPreferences sharedPreferences;
    private Member member;
    private ImageButton btnEditAccount;
    private ImageView profileIcon;
    private LinearLayout linearLayoutEmail, linearLayoutPhoneNumber, linearLayoutDni;
    private DateTimeFormatter outputFormatter, inputFormatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_details_member, container, false);

        loadComponents();

        btnEditAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showPoPup = new Intent(getContext(), PopupEditAccountActivity.class);
                showPoPup.putExtra("member", member);
                startActivity(showPoPup);
            }
        });

        return rootView;
    }

    private void loadComponents() {
        outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        linearLayoutEmail = rootView.findViewById(R.id.linearLayoutEmail);
        linearLayoutPhoneNumber = rootView.findViewById(R.id.linearLayoutPhoneNumber);
        linearLayoutDni = rootView.findViewById(R.id.linearLayoutDni);
        profileIcon = rootView.findViewById(R.id.profileIcon);
        btnEditAccount = rootView.findViewById(R.id.btnEditAccount);
        tvEmail = rootView.findViewById(R.id.tvEmail);
        tvNameAndSurname = rootView.findViewById(R.id.tvNameAndSurname);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        tvPhoneNumber = rootView.findViewById(R.id.tvPhoneNumber);
        tvDni = rootView.findViewById(R.id.tvDni);
        tvBirthdate = rootView.findViewById(R.id.tvBirthdate);
        tvLevel = rootView.findViewById(R.id.tvLevel);

        sharedPreferences = getActivity().getApplication().getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        member = (Member) getArguments().getSerializable("member");

        tvNameAndSurname.setText(member.getName() + " " + member.getSurname());
        tvUsername.setText(member.getUsername());


        LocalDate birthDate = LocalDate.parse(member.getBirthdate(), inputFormatter);

        tvBirthdate.setText(outputFormatter.format(birthDate));

        tvLevel.setText("Level " + member.getLevel().toString());

        if(member.getId().equals(sharedPreferences.getString("id", ""))){
            linearLayoutEmail.setVisibility(View.VISIBLE);
            linearLayoutPhoneNumber.setVisibility(View.VISIBLE);
            linearLayoutDni.setVisibility(View.VISIBLE);
            tvEmail.setText(sharedPreferences.getString("email", ""));
            tvPhoneNumber.setText(member.getPhone());
            tvDni.setText(member.getDni());
            btnEditAccount.setVisibility(View.VISIBLE);
        }

        tvNameAndSurname.setMaxWidth(getResources().getDisplayMetrics().widthPixels-300);


        if (member.getPhoto() != null && !member.getPhoto().isEmpty()) {
            Picasso.get()
                    .load(member.getPhoto())
                    .into(profileIcon);
        }

    }

}