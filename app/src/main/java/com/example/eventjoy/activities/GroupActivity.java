package com.example.eventjoy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.eventjoy.R;
import com.example.eventjoy.adapters.ViewPagerAdapter;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.enums.UserGroupRole;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

public class GroupActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private Bundle getData;
    private Group group;
    private ImageView groupIcon;
    private TextView tvTitle;
    private String userGroupRole;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();


        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(this, userGroupRole, group);
        viewPager2.setAdapter(viewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        toolbarActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsGroupIntent = new Intent(getApplicationContext(), DetailsGroupActivity.class);
                detailsGroupIntent.putExtra("group", group);
                detailsGroupIntent.putExtra("userGroupRole", userGroupRole);
                startActivity(detailsGroupIntent);
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents() {
        toolbarActivity = findViewById(R.id.toolbarActivity);
        groupIcon = findViewById(R.id.groupIcon);
        tvTitle = findViewById(R.id.tvTitle);
        getData = getIntent().getExtras();
        userGroupRole = getData.getString("userGroupRole");
        group = (Group) getData.getSerializable("group");
        tvTitle.setText(group.getTitle());

        if (group.getIcon() != null && !group.getIcon().isEmpty()) {
            Picasso.get()
                    .load(group.getIcon())
                    .placeholder(R.drawable.default_profile_photo)
                    .into(groupIcon);
        }
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

}