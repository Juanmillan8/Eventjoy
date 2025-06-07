package com.example.eventjoy.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.eventjoy.R;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;

public class PopupMemberOptionsActivity extends AppCompatActivity {

    private TextView tvMemberDetails, tvEvents, tvReports, tvValorations, tvAssignAdmin, tvExpelMember;
    private Bundle getData;
    private Member member;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popup_member_options);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();
        loadWindow();


        tvMemberDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsMemberIntent = new Intent(getApplicationContext(), DetailsMemberContainerActivity.class);
                detailsMemberIntent.putExtra("member", member);
                startActivity(detailsMemberIntent);
            }
        });

        tvValorations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listValorationsIntent = new Intent(getApplicationContext(), ListValorationsContainerActivity.class);
                listValorationsIntent.putExtra("member", member);
                startActivity(listValorationsIntent);
            }
        });

        tvEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO desde aqui es la nueva
                Intent listEventsIntent = new Intent(getApplicationContext(), ListEventsContainerActivity.class);
                listEventsIntent.putExtra("member", member);
                startActivity(listEventsIntent);
            }
        });

    }

    private void loadWindow(){
        DisplayMetrics windowsMeasurements = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(windowsMeasurements);

        this.setFinishOnTouchOutside(true);

        int width = windowsMeasurements.widthPixels;
        int tall = windowsMeasurements.heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().setBackgroundBlurRadius(1);
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(role.equals("ADMIN")){
                getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.22));
                tvExpelMember.setVisibility(View.VISIBLE);
                tvAssignAdmin.setVisibility(View.VISIBLE);
            }else{
                getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.15));
            }

        }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setLayout((int) (width * 0.50), (int) (tall * 0.20));
        }
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
    }

    private void loadComponents(){
        tvMemberDetails = findViewById(R.id.tvMemberDetails);
        tvEvents = findViewById(R.id.tvEvents);
        tvReports = findViewById(R.id.tvReports);
        tvValorations = findViewById(R.id.tvValorations);
        tvAssignAdmin = findViewById(R.id.tvAssignAdmin);
        tvExpelMember = findViewById(R.id.tvExpelMember);
        getData = getIntent().getExtras();
        member = (Member) getData.getSerializable("member");
        role = getData.getString("role");
    }

}