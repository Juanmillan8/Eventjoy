package com.example.eventjoy.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.eventjoy.R;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.callbacks.UserGroupRoleCallback;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.EventService;
import com.example.eventjoy.services.UserGroupService;

import java.util.concurrent.atomic.AtomicInteger;

public class PopupMemberOptionsActivity extends AppCompatActivity {

    private TextView tvMemberDetails, tvEvents, tvReports, tvValorations, tvAssignRemoveAdmin, tvExpelMember;
    private Bundle getData;
    private Member member;
    private String role;
    private UserGroupService userGroupService;
    private Group group;
    private EventService eventService;

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

        loadServices();
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
                Intent listEventsIntent = new Intent(getApplicationContext(), ListEventsContainerActivity.class);
                listEventsIntent.putExtra("member", member);
                startActivity(listEventsIntent);
            }
        });

        tvReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listReportsIntent = new Intent(getApplicationContext(), ListReportsContainerActivity.class);
                listReportsIntent.putExtra("member", member);
                listReportsIntent.putExtra("group", group);
                startActivity(listReportsIntent);
            }
        });

        tvAssignRemoveAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userGroupService.asignAdmin(group.getId(), member.getId(), new SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (message != null) {
                            if (tvAssignRemoveAdmin.getText().equals("Remove administrator")) {
                                tvAssignRemoveAdmin.setText("Assign administrator");
                            } else {
                                tvAssignRemoveAdmin.setText("Remove administrator");
                            }
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(String errorMessage) {
                        Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tvExpelMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventService.isUserRegisteredInOngoingEvent(group.getId(), member.getId(), new SimpleCallback() {
                    @Override
                    public void onSuccess(String onSuccess) {
                        if (onSuccess.equals("true")) {
                            Toast.makeText(getApplicationContext(),"Cannot kick: The user is in an ongoing event", Toast.LENGTH_LONG).show();
                        } else {
                            userGroupService.deleteUserGroup(group.getId(), member.getId(), new SimpleCallback() {
                                @Override
                                public void onSuccess(String onSuccess) {
                                    if(onSuccess!=null){
                                        Toast.makeText(getApplicationContext(),"User successfully kicked out of group", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(String onCancelledMessage) {
                                    Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(String onCancelledMessage) {
                        Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                    }
                });
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
            if (role.equals("ADMIN")) {
                getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.22));
                tvExpelMember.setVisibility(View.VISIBLE);
                tvAssignRemoveAdmin.setVisibility(View.VISIBLE);

                userGroupService.checkUserGroupRole(group.getId(), member.getId(), new UserGroupRoleCallback() {
                    @Override
                    public void onSuccess(UserGroupRole u) {
                        if (u.name().equals("ADMIN")) {
                            tvAssignRemoveAdmin.setText("Remove administrator");
                        } else {
                            tvAssignRemoveAdmin.setText("Assign administrator");
                        }
                    }

                    @Override
                    public void onCancelled(String onCancelledMessage) {
                        Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.15));
            }

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (role.equals("ADMIN")) {
                getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.43));
                tvExpelMember.setVisibility(View.VISIBLE);
                tvAssignRemoveAdmin.setVisibility(View.VISIBLE);

                userGroupService.checkUserGroupRole(group.getId(), member.getId(), new UserGroupRoleCallback() {
                    @Override
                    public void onSuccess(UserGroupRole u) {
                        if (u.name().equals("ADMIN")) {
                            tvAssignRemoveAdmin.setText("Remove administrator");
                        } else {
                            tvAssignRemoveAdmin.setText("Assign administrator");
                        }
                    }

                    @Override
                    public void onCancelled(String onCancelledMessage) {
                        Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                getWindow().setLayout((int) (width * 0.70), (int) (tall * 0.30));
            }
        }
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
    }

    private void loadComponents() {
        tvMemberDetails = findViewById(R.id.tvMemberDetails);
        tvEvents = findViewById(R.id.tvEvents);
        tvReports = findViewById(R.id.tvReports);
        tvValorations = findViewById(R.id.tvValorations);
        tvAssignRemoveAdmin = findViewById(R.id.tvAssignRemoveAdmin);
        tvExpelMember = findViewById(R.id.tvExpelMember);
        getData = getIntent().getExtras();
        member = (Member) getData.getSerializable("member");
        group = (Group) getData.getSerializable("group");
        role = getData.getString("role");
    }

    private void loadServices() {
        userGroupService = new UserGroupService(getApplicationContext());
        eventService = new EventService(getApplicationContext());
    }

}