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
    private Bundle getMember;
    private Member member;

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

        loadWindow();
        loadComponents();

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
            getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.28));
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
        getMember = getIntent().getExtras();
        member = (Member) getMember.getSerializable("member");
    }

}