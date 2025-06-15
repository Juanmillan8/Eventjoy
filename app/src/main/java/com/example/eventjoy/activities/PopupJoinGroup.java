package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Invitation;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.UserGroup;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.InvitationService;
import com.example.eventjoy.services.UserGroupService;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PopupJoinGroup extends AppCompatActivity {

    private Bundle getData;
    private Group group;
    private Invitation invitation;
    private TextView tvGroupTitle, tvInformativeText;
    private ImageView iconGroup;
    private Button btnJoinGroup;
    private SharedPreferences sharedPreferences;
    private UserGroupService userGroupService;
    private InvitationService invitationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popup_join_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadWindow();
        loadComponents();

        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDateTime localDateTime = LocalDateTime.now().withNano(0);
                ZonedDateTime utcDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
                String formattedToday = utcDateTime.format(DateTimeFormatter.ISO_INSTANT);

                if(invitation!=null){
                    invitationService.deleteInvitation(invitation);
                }

                UserGroup us = new UserGroup();
                us.setGroupId(group.getId());
                us.setAdmin(false);
                us.setJoinedAt(formattedToday);
                us.setUserId(sharedPreferences.getString("id", ""));
                us.setNotificationsEnabled(true);
                userGroupService.insertUserGroup(us);

                Intent groupMainIntent = new Intent(getApplicationContext(), GroupActivity.class);
                groupMainIntent.putExtra("group", group);
                groupMainIntent.putExtra("userGroupRole", UserGroupRole.PARTICIPANT.name());
                startActivity(groupMainIntent);
                Toast.makeText(getApplicationContext(), "You have successfully joined the group", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void loadWindow() {
        DisplayMetrics windowsMeasurements = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(windowsMeasurements);

        this.setFinishOnTouchOutside(true);

        int width = windowsMeasurements.widthPixels;
        int tall = windowsMeasurements.heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().setBackgroundBlurRadius(1);
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().setLayout((int) (width * 0.90), (int) (tall * 0.50));
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.89));
        }
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
    }

    private void loadComponents() {
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        tvGroupTitle = findViewById(R.id.tvGroupTitle);
        iconGroup = findViewById(R.id.iconGroup);
        tvInformativeText = findViewById(R.id.tvInformativeText);

        getData = getIntent().getExtras();
        group = (Group) getData.getSerializable("group");
        invitation = (Invitation) getData.getSerializable("invitation");

        if (group.getIcon() != null && !group.getIcon().isEmpty()) {
            Picasso.get().load(group.getIcon()).into(iconGroup);
        }

        tvGroupTitle.setText("Group: " + group.getTitle());
        tvInformativeText.setText("You must join the " + group.getTitle() + " group to view its content and participate");
    }

    private void loadServices(){
        userGroupService = new UserGroupService(getApplicationContext());
        invitationService = new InvitationService(getApplicationContext());
    }

}