package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.ValorationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;

public class CreateValorationsActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private Bundle getMember;
    private Member member;
    private TextView tvUsername;
    private ImageView ivUserIcon;
    private Button btnCreateValoration;
    private TextInputEditText textInputEditTextTitle, textInputEditTextDescription;
    private RatingBar ratingBar;
    private SharedPreferences sharedPreferences;
    private ValorationService valorationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_valorations);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

        btnCreateValoration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifications();
            }
        });

    }

    private void verifications(){
        if (textInputEditTextTitle.getText().toString().isBlank()) {
            Toast.makeText(getApplicationContext(), "You must fill out all the required fields", Toast.LENGTH_SHORT).show();
        } else {
            createValoration();
        }
    }

    private void createValoration(){

        Valoration valoration = new Valoration();
        valoration.setTitle(textInputEditTextTitle.getText().toString());
        valoration.setDescription(textInputEditTextDescription.getText().toString());
        valoration.setRating((double) ratingBar.getRating());
        valoration.setRatedUserId(member.getId());
        valoration.setRaterUserId(sharedPreferences.getString("id", ""));

        valorationService.insertValoration(valoration);
        Toast.makeText(getApplicationContext(), "Valoration created correctly", Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents(){
        ratingBar = findViewById(R.id.ratingBar);
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        textInputEditTextTitle = findViewById(R.id.textInputEditTextTitle);
        textInputEditTextDescription = findViewById(R.id.textInputEditTextDescription);
        btnCreateValoration = findViewById(R.id.btnCreateValoration);
        ivUserIcon = findViewById(R.id.ivUserIcon);
        tvUsername = findViewById(R.id.tvUsername);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getMember = getIntent().getExtras();
        member = (Member) getMember.getSerializable("member");
        tvUsername.setText(member.getUsername());

        if (member.getPhoto() != null && !member.getPhoto().isEmpty()) {
            Picasso.get()
                    .load(member.getPhoto())
                    .placeholder(R.drawable.default_profile_photo)
                    .into(ivUserIcon);
        }
    }

    private void loadServices(){
        valorationService = new ValorationService(getApplicationContext());
    }

}