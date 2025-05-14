package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class ModifyPasswordActivity extends AppCompatActivity {

    private TextInputEditText textInputEditTextCurrentPassword, textInputEditTextNewPassword, textInputEditTextConfirmNewPassword;
    private Button btnChangePassword;
    private Toolbar toolbarActivity;
    private SharedPreferences sharedPreferences;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modify_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadComponents();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

    }

    private void changePassword() {

        if (textInputEditTextCurrentPassword.getText().toString().isBlank()) {
            Toast.makeText(this, "To change your password you must first enter your current password", Toast.LENGTH_SHORT).show();
        } else if (!textInputEditTextCurrentPassword.getText().toString().isBlank() && (textInputEditTextNewPassword.getText().toString().isBlank() || textInputEditTextConfirmNewPassword.getText().toString().isBlank())) {
            Toast.makeText(this, "If you want to change your password you must enter a new password and confirm it", Toast.LENGTH_SHORT).show();
        } else {
            if (!textInputEditTextNewPassword.getText().toString().equals(textInputEditTextConfirmNewPassword.getText().toString())) {
                Toast.makeText(this, "The new password does not match the new password confirmation", Toast.LENGTH_SHORT).show();
            } else {
                AuthCredential credential = EmailAuthProvider.getCredential(sharedPreferences.getString("email", ""), textInputEditTextCurrentPassword.getText().toString());
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.reload();
                            user.updatePassword(textInputEditTextNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "The password has been successfully changed", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                        Toast.makeText(getApplicationContext(), "The new password is not secure", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthException) {
                            FirebaseAuthException authException = (FirebaseAuthException) e;
                            String errorCode = authException.getErrorCode();
                            switch (errorCode) {
                                case "ERROR_WRONG_PASSWORD":
                                    Toast.makeText(getApplicationContext(), "The current password you have entered is incorrect", Toast.LENGTH_SHORT).show();
                                    break;
                                case "ERROR_USER_DISABLED":
                                    Toast.makeText(getApplicationContext(), "The user is disabled", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void loadComponents() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textInputEditTextCurrentPassword = findViewById(R.id.textInputEditTextCurrentPassword);
        textInputEditTextNewPassword = findViewById(R.id.textInputEditTextNewPassword);
        textInputEditTextConfirmNewPassword = findViewById(R.id.textInputEditTextConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
    }
}