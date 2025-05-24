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

import com.example.eventjoy.R;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Member;
import com.squareup.picasso.Picasso;

public class GroupActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private Bundle getGroup;
    private Group group;
    private ImageView groupIcon;
    private TextView tvTitle;

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

        toolbarActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsGroupIntent = new Intent(getApplicationContext(), DetailsGroupActivity.class);
                detailsGroupIntent.putExtra("group", group);
                startActivity(detailsGroupIntent);
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
        getGroup = getIntent().getExtras();
        group = (Group) getGroup.getSerializable("group");
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