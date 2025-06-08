package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventjoy.R;
import com.example.eventjoy.adapters.MemberAdapter;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.callbacks.SimpleCallback;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Invitation;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.InvitationService;
import com.example.eventjoy.services.MemberService;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ListMembersActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private RecyclerView lvMembers;
    private MemberService memberService;
    private MemberAdapter memberAdapter;
    private Bundle getGroup;
    private Group group;
    private InvitationService invitationService;
    private SharedPreferences sharedPreferences;
    private String idCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_members);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningMembers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        memberService.stopListening();
    }

    private void startListeningMembers() {
        memberService.stopListening();
        memberService.getMembersNotInGroup(group.getId(), new MembersCallback() {
            @Override
            public void onSuccess(List<Member> members) {
                memberAdapter = new MemberAdapter(getApplicationContext(), members, true);
                addClickListenerToAdapter();
                lvMembers.setAdapter(memberAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addClickListenerToAdapter(){
        memberAdapter.setOnMemberClickListener(member -> {

            invitationService.hasAlreadyInvited(member.getId(), group.getId(), new SimpleCallback() {
                @Override
                public void onSuccess(String onSuccess) {
                    if(onSuccess.equals("true")){
                        Toast.makeText(getApplicationContext(),"The user already has a pending invitation", Toast.LENGTH_SHORT).show();
                    }else{
                        Invitation invitation = new Invitation();
                        invitation.setInviterUserId(idCurrentUser);
                        invitation.setInvitedUserId(member.getId());
                        invitation.setGroupId(group.getId());

                        ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                        String formattedToday = today.format(formatter);

                        invitation.setInvitedAt(formattedToday);

                        invitationService.insertInvitation(invitation);
                        Toast.makeText(getApplicationContext(),"Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(String onCancelledMessage) {
                    Toast.makeText(getApplicationContext(), onCancelledMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents(){
        getGroup = getIntent().getExtras();
        group = (Group) getGroup.getSerializable("group");
        List<Member> memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(getApplicationContext(), memberList, true);
        lvMembers = findViewById(R.id.lvMembers);
        lvMembers.setLayoutManager(new LinearLayoutManager(this));
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addClickListenerToAdapter();
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
    }

    private void loadServices(){
        memberService = new MemberService(getApplicationContext());
        invitationService = new InvitationService(getApplicationContext());
    }

}