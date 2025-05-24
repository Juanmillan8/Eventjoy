package com.example.eventjoy.activities;

import android.content.Intent;
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
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailsGroupActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Bundle getGroup;
    private Group group;
    private TextView tvTitle, tvDescription;
    private ImageView groupIcon, ivBack;
    private ListView lvMembers;
    private MemberAdapter memberAdapter;
    private ArrayList<Member> members;
    private SearchView svMembers;
    private TextView tvMembership;

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

        svMembers.setOnQueryTextListener(this);

    }

    private void loadComponents() {
        tvMembership = findViewById(R.id.tvMembership);
        svMembers = findViewById(R.id.svMembers);
        lvMembers = findViewById(R.id.lvMembers);
        ivBack = findViewById(R.id.ivBack);
        tvDescription = findViewById(R.id.tvDescription);
        tvTitle = findViewById(R.id.tvTitle);
        groupIcon = findViewById(R.id.groupIcon);
        getGroup = getIntent().getExtras();
        group = (Group) getGroup.getSerializable("group");
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
        members = new ArrayList<>();
        Member member1 = new Member();
        member1.setId("idmember1");
        member1.setName("Juan");
        member1.setSurname("Martínez");
        member1.setUsername("juanito");
        member1.setDni("12345678A");
        member1.setPhone("600123456");
        member1.setBirthdate("1995-04-12");
        member1.setLevel(1);
        member1.setProvider(Provider.EMAIL);

        Member member2 = new Member();
        member2.setId("idmember2");
        member2.setName("Carlos");
        member2.setSurname("Gómez");
        member2.setUsername("mesi");
        member2.setDni("87654321B");
        member2.setPhone("600654321");
        member2.setBirthdate("1992-09-08");
        member2.setLevel(2);
        member2.setProvider(Provider.GOOGLE);

        members.add(member1);
        members.add(member2);

        memberAdapter = new MemberAdapter(getApplicationContext(), members);
        lvMembers.setAdapter(memberAdapter);
        configureSearchView();
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

        tvMembership.setText(members.size() + " members");
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