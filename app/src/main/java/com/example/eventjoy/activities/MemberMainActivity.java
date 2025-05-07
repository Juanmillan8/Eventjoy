package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.MemberService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class MemberMainActivity extends AppCompatActivity {

    private Button btnLogout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private Member member;
    private ImageView profileIcon;
    private MemberService memberService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        Log.i("1", "1");
        loadComponents();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

    }

    private void logout() {
        editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        mAuth.signOut();

        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }


    private void loadComponents(){
        profileIcon = findViewById(R.id.profileIcon);
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        btnLogout = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();
        Log.i("2", "2");
        memberService.getMemberById(sharedPreferences.getString("id", ""), new OnSuccessListener<Member>() {
            @Override
            public void onSuccess(Member memberGet) {
                Log.i("3", "3");
                member = memberGet;

                if(member!=null){
                    Log.i("NONUL", "NONUL");
                }else{
                    Log.i("NUL", "NUL");
                }

                if(member.getPhoto()!=null){
                    Log.i("4", "4");
                    Picasso.get().load(Uri.parse(member.getPhoto())).into(profileIcon);
                }
                Log.i("5", "5");
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadServices(){
        memberService = new MemberService(getApplicationContext());
    }

}