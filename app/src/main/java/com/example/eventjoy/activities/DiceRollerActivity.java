package com.example.eventjoy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.util.Random;

public class DiceRollerActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private ImageView ivDice1, ivDice2, ivDice3, ivDice4, ivDice5, ivDice6, ivDice7, ivDice8, ivDice9, ivDice10;
    private ImageButton ibAddDice, ibRemoveDice;
    private TextView tvNumberDice, tvTotal;
    private Integer numberDice;
    private Button btnRollDice;

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

        btnRollDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throwDice();
            }
        });

    }

    private void throwDice(){
        if(numberDice!=null || numberDice>0){
            Integer accumulator = 0;
            for (int i=0;i<numberDice;i++){
                Random random = new Random();
                int randomNumber = random.nextInt(6) + 1;
                accumulator += randomNumber;

                switch (i) {
                    case 0:
                        switch (randomNumber) {
                            case 1:
                                ivDice1.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice1.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice1.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice1.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice1.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice1.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 1:
                        switch (randomNumber) {
                            case 1:
                                ivDice2.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice2.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice2.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice2.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice2.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice2.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 2:
                        switch (randomNumber) {
                            case 1:
                                ivDice3.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice3.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice3.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice3.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice3.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice3.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 3:
                        switch (randomNumber) {
                            case 1:
                                ivDice4.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice4.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice4.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice4.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice4.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice4.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 4:
                        switch (randomNumber) {
                            case 1:
                                ivDice5.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice5.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice5.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice5.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice5.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice5.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 5:
                        switch (randomNumber) {
                            case 1:
                                ivDice6.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice6.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice6.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice6.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice6.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice6.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 6:
                        switch (randomNumber) {
                            case 1:
                                ivDice7.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice7.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice7.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice7.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice7.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice7.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 7:
                        switch (randomNumber) {
                            case 1:
                                ivDice8.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice8.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice8.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice8.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice8.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice8.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 8:
                        switch (randomNumber) {
                            case 1:
                                ivDice9.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice9.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice9.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice9.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice9.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice9.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                    case 9:
                        switch (randomNumber) {
                            case 1:
                                ivDice10.setImageResource(R.drawable.icon_dice_1_point);
                                break;
                            case 2:
                                ivDice10.setImageResource(R.drawable.icon_dice_2_points);
                                break;
                            case 3:
                                ivDice10.setImageResource(R.drawable.icon_dice_3_points);
                                break;
                            case 4:
                                ivDice10.setImageResource(R.drawable.icon_dice_4_points);
                                break;
                            case 5:
                                ivDice10.setImageResource(R.drawable.icon_dice_5_points);
                                break;
                            case 6:
                                ivDice10.setImageResource(R.drawable.icon_dice_6_points);
                                break;
                        }
                        break;
                }

            }
            tvTotal.setText("Total: " + accumulator);
        }
    }

    private void addDice(){
        Log.i(String.valueOf(ivDice1.getVisibility()), String.valueOf(View.GONE));
        if(ivDice1.getVisibility() == View.GONE){
            numberDice = 1;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice1.setVisibility(View.VISIBLE);
        }else if (ivDice2.getVisibility() == View.GONE){
            numberDice = 2;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice2.setVisibility(View.VISIBLE);
        }else if (ivDice3.getVisibility() == View.GONE){
            numberDice = 3;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice3.setVisibility(View.VISIBLE);
        }else if (ivDice4.getVisibility() == View.GONE){
            numberDice = 4;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice4.setVisibility(View.VISIBLE);
        }else if (ivDice5.getVisibility() == View.GONE){
            numberDice = 5;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice5.setVisibility(View.VISIBLE);
        }else if (ivDice6.getVisibility() == View.GONE){
            numberDice = 6;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice6.setVisibility(View.VISIBLE);
        }else if (ivDice7.getVisibility() == View.GONE){
            numberDice = 7;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice7.setVisibility(View.VISIBLE);
        }else if (ivDice8.getVisibility() == View.GONE){
            numberDice = 8;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice8.setVisibility(View.VISIBLE);
        }else if (ivDice9.getVisibility() == View.GONE){
            numberDice = 9;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice9.setVisibility(View.VISIBLE);
        }else if (ivDice10.getVisibility() == View.GONE){
            numberDice = 10;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice10.setVisibility(View.VISIBLE);
        }
    }

    private void removeDice(){
        if(ivDice10.getVisibility() == View.VISIBLE){
            numberDice = 9;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice10.setVisibility(View.GONE);
        }else if (ivDice9.getVisibility() == View.VISIBLE){
            numberDice = 8;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice9.setVisibility(View.GONE);
        }else if (ivDice8.getVisibility() == View.VISIBLE){
            numberDice = 7;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice8.setVisibility(View.GONE);
        }else if (ivDice7.getVisibility() == View.VISIBLE){
            numberDice = 6;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice7.setVisibility(View.GONE);
        }else if (ivDice6.getVisibility() == View.VISIBLE){
            numberDice = 5;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice6.setVisibility(View.GONE);
        }else if (ivDice5.getVisibility() == View.VISIBLE){
            numberDice = 4;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice5.setVisibility(View.GONE);
        }else if (ivDice4.getVisibility() == View.VISIBLE){
            numberDice = 3;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice4.setVisibility(View.GONE);
        }else if (ivDice3.getVisibility() == View.VISIBLE){
            numberDice = 2;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice3.setVisibility(View.GONE);
        }else if (ivDice2.getVisibility() == View.VISIBLE){
            numberDice = 1;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice2.setVisibility(View.GONE);
        }else if (ivDice1.getVisibility() == View.VISIBLE){
            numberDice = 0;
            tvNumberDice.setText("Dice: " +numberDice);
            ivDice1.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents(){
        btnRollDice = findViewById(R.id.btnRollDice);
        tvNumberDice = findViewById(R.id.tvNumberDice);
        tvTotal = findViewById(R.id.tvTotal);
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