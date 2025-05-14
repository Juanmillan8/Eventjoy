package com.example.eventjoy.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.eventjoy.R;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.models.Member;
import com.google.firebase.auth.FirebaseAuth;

public class PopupEditAccountActivity extends AppCompatActivity {

    private TextView tvModifyData, tvModifyPassword;
    private Member memberEdit;
    private Bundle getMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popup_edit_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadWindow();
        loadComponents();

        tvModifyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(memberEdit.getProvider().equals(Provider.GOOGLE)){
                    Toast.makeText(getApplicationContext(), "A user logged in with Google cannot change their password", Toast.LENGTH_SHORT).show();
                }else{
                    Intent modifyPasswordIntent = new Intent(getApplicationContext(), ModifyPasswordActivity.class);
                    startActivity(modifyPasswordIntent);
                    finish();
                }
            }
        });

        tvModifyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editMemberIntent = new Intent(getApplicationContext(), EditMemberActivity.class);
                editMemberIntent.putExtra("member", memberEdit);
                startActivity(editMemberIntent);
                finish();
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
            getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.10));
        }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setLayout((int) (width * 0.50), (int) (tall * 0.20));
        }
         getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
    }

    private void loadComponents(){
        tvModifyData = findViewById(R.id.tvModifyData);
        tvModifyPassword = findViewById(R.id.tvModifyPassword);
        getMember = getIntent().getExtras();
        memberEdit = (Member) getMember.getSerializable("member");
    }

}