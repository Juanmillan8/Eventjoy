package com.example.eventjoy.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.fragments.DetailsMemberFragment;
import com.example.eventjoy.fragments.ListValorationsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ListValorationsContainerActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_valorations_container);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();

        ListValorationsFragment listValorationsFragment = new ListValorationsFragment();
        listValorationsFragment.setArguments(data);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_list_valorations, listValorationsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

}