package com.example.eventjoy.activities;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class PopupReauthenticateActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    private Button btnReauthenticate;
    private static FirebaseUser user;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popup_reauthenticate);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadWindow();
        loadComponents();

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnReauthenticate.setOnClickListener(v -> {
            reauthenticate();
        });

    }

    private void reauthenticate(){
        //Verificar que los campos de email y contraseña no estén vacíos
        if (textInputEditTextEmail.getText().toString().isBlank() || textInputEditTextPassword.getText().toString().isBlank()) {
            Toast.makeText(this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
        }else{
            //Crear las credenciales de autenticación con el email y la contraseña proporcionados
            AuthCredential credential = EmailAuthProvider.getCredential(textInputEditTextEmail.getText().toString(),textInputEditTextPassword.getText().toString());

            //Reautenticar al usuario con las credenciales proporcionadas
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Si la reautenticación es exitosa, comprobamos que haya un usuario logado en la aplicación, si es así recargamos dicho usuario para
                        //asegurar que los cambios en el estado de la cuenta se reflejen en la aplicación
                        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                            user.reload();
                        }else{
                            //Puede dar la ocasión de que el usuario haya insertado unas credenciales incorrectas y haya sido deslogado de la aplicación, si
                            //posteriormente introduce sus credenciales correctamente, tendremos que llamar al meto_do signInWithEmailAndPassword para que
                            //inicie sesión de nuevo
                            mAuth.signInWithEmailAndPassword(textInputEditTextEmail.getText().toString(), textInputEditTextPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                }
                            });
                        }
                        finish();
                        Toast.makeText(getApplicationContext(), "Successfully reauthenticated", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Manejo de errores si la reautenticación falla
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();

                        if(errorCode.equalsIgnoreCase("ERROR_WRONG_PASSWORD") || errorCode.equalsIgnoreCase("ERROR_USER_NOT_FOUND") ||
                                errorCode.equalsIgnoreCase("ERROR_INVALID_EMAIL") || errorCode.equalsIgnoreCase("ERROR_WEAK_PASSWORD") ||
                                errorCode.equalsIgnoreCase("ERROR_USER_MISMATCH")){
                            Toast.makeText(getApplicationContext(), "Incorrect email or password", Toast.LENGTH_SHORT).show();
                        }else if(errorCode.equalsIgnoreCase("ERROR_USER_DISABLED")){
                            Toast.makeText(getApplicationContext(), "The user is disabled", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.e("Error - PopupReauthenticateActivity - reauthenticate", e.getMessage());
                            Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Error - PopupReauthenticateActivity - reauthenticate", e.getMessage());
                        Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                    }

                    //Cerrar sesión del usuario si falla al reautenticarse
                    mAuth.signOut();

                }
            });
        }
    }

    private void loadWindow(){
        DisplayMetrics windowsMeasurements = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(windowsMeasurements);

        int width = windowsMeasurements.widthPixels;
        int tall = windowsMeasurements.heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().setBackgroundBlurRadius(1);
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            getWindow().setLayout((int) (width * 0.95), (int) (tall * 0.45));
        }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setLayout((int) (width * 0.60), (int) (tall * 0.90));
        }
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
    }

    private void loadComponents(){
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        btnReauthenticate = findViewById(R.id.btnReauthenticate);
        mAuth = FirebaseAuth.getInstance();
        btnBack = findViewById(R.id.btnBack);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
    }
}