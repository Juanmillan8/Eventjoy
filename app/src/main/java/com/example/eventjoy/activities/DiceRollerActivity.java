package com.example.eventjoy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;

public class DiceRollerActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private ImageView ivDice1, ivDice2, ivDice3, ivDice4, ivDice5, ivDice6, ivDice7, ivDice8, ivDice9, ivDice10;
    private ImageButton ibAddDice, ibRemoveDice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dice_roller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        ibAddDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDice();
            }
        });

        ibRemoveDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDice();
            }
        });

    }

    private void addDice(){
        Log.i(String.valueOf(ivDice1.getVisibility()), String.valueOf(View.GONE));
        if(ivDice1.getVisibility() == View.GONE){
            Log.i("1", "1");
            ivDice1.setVisibility(View.VISIBLE);
        }else if (ivDice2.getVisibility() == View.GONE){
            Log.i("2", "2");
            ivDice2.setVisibility(View.VISIBLE);
        }else if (ivDice3.getVisibility() == View.GONE){
            Log.i("3", "3");
            ivDice3.setVisibility(View.VISIBLE);
        }else if (ivDice4.getVisibility() == View.GONE){
            Log.i("4", "4");
            ivDice4.setVisibility(View.VISIBLE);
        }else if (ivDice5.getVisibility() == View.GONE){
            Log.i("5", "5");
            ivDice5.setVisibility(View.VISIBLE);
        }else if (ivDice6.getVisibility() == View.GONE){
            Log.i("6", "6");
            ivDice6.setVisibility(View.VISIBLE);
        }else if (ivDice7.getVisibility() == View.GONE){
            Log.i("7", "7");
            ivDice7.setVisibility(View.VISIBLE);
        }else if (ivDice8.getVisibility() == View.GONE){
            Log.i("8", "8");
            ivDice8.setVisibility(View.VISIBLE);
        }else if (ivDice9.getVisibility() == View.GONE){
            Log.i("9", "9");
            ivDice9.setVisibility(View.VISIBLE);
        }else if (ivDice10.getVisibility() == View.GONE){
            Log.i("10", "10");
            ivDice10.setVisibility(View.VISIBLE);
        }else{
            Log.i("11", "11");
            Toast.makeText(getApplicationContext(), "The maximum number of dice is 10", Toast.LENGTH_SHORT).show();
        }
        Log.i("12", "12");
    }

    private void removeDice(){
        if(ivDice1.getVisibility() == View.VISIBLE){
            ivDice1.setVisibility(View.GONE);
        }else if (ivDice2.getVisibility() == View.VISIBLE){
            ivDice2.setVisibility(View.GONE);
        }else if (ivDice3.getVisibility() == View.VISIBLE){
            ivDice3.setVisibility(View.GONE);
        }else if (ivDice4.getVisibility() == View.VISIBLE){
            ivDice4.setVisibility(View.GONE);
        }else if (ivDice5.getVisibility() == View.VISIBLE){
            ivDice5.setVisibility(View.GONE);
        }else if (ivDice6.getVisibility() == View.VISIBLE){
            ivDice6.setVisibility(View.GONE);
        }else if (ivDice7.getVisibility() == View.VISIBLE){
            ivDice7.setVisibility(View.GONE);
        }else if (ivDice8.getVisibility() == View.VISIBLE){
            ivDice8.setVisibility(View.GONE);
        }else if (ivDice9.getVisibility() == View.VISIBLE){
            ivDice9.setVisibility(View.GONE);
        }else if (ivDice10.getVisibility() == View.VISIBLE){
            ivDice10.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents(){
        ibRemoveDice = findViewById(R.id.ibRemoveDice);
        ibAddDice = findViewById(R.id.ibAddDice);
        ivDice1 = findViewById(R.id.ivDice1);
        ivDice2 = findViewById(R.id.ivDice2);
        ivDice3 = findViewById(R.id.ivDice3);
        ivDice4 = findViewById(R.id.ivDice4);
        ivDice5 = findViewById(R.id.ivDice5);
        ivDice6 = findViewById(R.id.ivDice6);
        ivDice7 = findViewById(R.id.ivDice7);
        ivDice8 = findViewById(R.id.ivDice8);
        ivDice9 = findViewById(R.id.ivDice9);
        ivDice10 = findViewById(R.id.ivDice10);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}