package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.eventjoy.R;
import com.example.eventjoy.adapters.GroupAdapter;
import com.example.eventjoy.adapters.MemberAdapter;
import com.example.eventjoy.callbacks.GroupsCallback;
import com.example.eventjoy.callbacks.MembersCallback;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.fragments.HomeMemberFragment;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.GroupService;
import com.example.eventjoy.services.UserGroupService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailsGroupActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Bundle getData;
    private Group group;
    private String userGroupRole;
    private TextView tvTitle, tvDescription;
    private ImageView groupIcon, ivBack;
    private ListView lvMembers;
    private MemberAdapter memberAdapter;
    private SearchView svMembers;
    private TextView tvMembership;
    private UserGroupService userGroupService;
    private GroupService groupService;
    private SharedPreferences sharedPreferences;
    private String idCurrentUser;
    private LinearLayout linearLayoutEditGroup, linearLayoutDeleteGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lvMembers.setOnItemClickListener((parent, view, position, id) -> {
            Member member = (Member) parent.getItemAtPosition(position);
            Intent showPoPup = new Intent(getApplicationContext(), PopupMemberOptionsActivity.class);
            showPoPup.putExtra("member", member);
            startActivity(showPoPup);
        });

        linearLayoutDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupService.deleteGroup(group);
                Toast.makeText(getApplicationContext(), "Successfully deleted group", Toast.LENGTH_SHORT).show();
                Intent mainMemberActivity = new Intent(getApplicationContext(), MemberMainActivity.class);
                startActivity(mainMemberActivity);
            }
        });

        svMembers.setOnQueryTextListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningMembers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userGroupService.stopListening();
    }

    private void startListeningMembers(){
        userGroupService.stopListening();
        userGroupService.getMembersByGroupId(group.getId(), idCurrentUser, new MembersCallback() {
            @Override
            public void onSuccess(List<Member> members) {
                memberAdapter = new MemberAdapter(getApplicationContext(), members);
                lvMembers.setAdapter(memberAdapter);
                tvMembership.setText(members.size() + " members");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComponents() {
        linearLayoutEditGroup = findViewById(R.id.linearLayoutEditGroup);
        linearLayoutDeleteGroup = findViewById(R.id.linearLayoutDeleteGroup);
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        idCurrentUser = sharedPreferences.getString("id", "");
        tvMembership = findViewById(R.id.tvMembership);
        svMembers = findViewById(R.id.svMembers);
        lvMembers = findViewById(R.id.lvMembers);
        ivBack = findViewById(R.id.ivBack);
        tvDescription = findViewById(R.id.tvDescription);
        tvTitle = findViewById(R.id.tvTitle);
        groupIcon = findViewById(R.id.groupIcon);
        getData = getIntent().getExtras();
        userGroupRole = getData.getString("userGroupRole");
        group = (Group) getData.getSerializable("group");
        tvTitle.setText(group.getTitle());

        if (group.getDescription() != null && !group.getDescription().toString().isBlank()) {
            tvDescription.setText(group.getDescription());
        } else {
            tvDescription.setText("This group does not yet have a description");
        }

        if (group.getIcon() != null && !group.getIcon().isEmpty()) {
            Picasso.get()
                    .load(group.getIcon())
                    .placeholder(R.drawable.default_profile_photo)
                    .into(groupIcon);
        }

        if(userGroupRole.equals("ADMIN")){
            linearLayoutDeleteGroup.setVisibility(View.VISIBLE);
            linearLayoutEditGroup.setVisibility(View.VISIBLE);
        }

        configureSearchView();
    }

    private void loadServices(){
        userGroupService = new UserGroupService(getApplicationContext());
        groupService = new GroupService(getApplicationContext());
    }

    private void configureSearchView() {
        int searchPlateId = svMembers.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = svMembers.findViewById(searchPlateId);
        searchEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.grayBluish));
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.hint));
        searchEditText.setTextSize(16);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        memberAdapter.filter(newText);
        return false;
    }
}