package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.eventjoy.R;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.enums.Role;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.MemberService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private TextView tvSignUp;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Button btnLogin, btnSignInGoogle;
    private TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    private MemberService memberService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final int REQ_CODE_GOOGLE_SIGN_IN = 1;
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInOptions googleSignInOptions;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        loadServices();
        loadComponents();

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        if (user != null) {
            if (role.equals(Role.MEMBER.name())) {
                Intent memberMainIntent = new Intent(getApplicationContext(), MemberMainActivity.class);
                startActivity(memberMainIntent);
            }else if (role.equals(Role.ADMIN.name())) {
                Intent adminMainIntent = new Intent(getApplicationContext(), AdminMainActivity.class);
                startActivity(adminMainIntent);
            }else{
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Signed in with Google. Finish registration to use the app.", Toast.LENGTH_SHORT).show();
            }
        } else {
            editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
        }

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInClient.signOut();
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, REQ_CODE_GOOGLE_SIGN_IN);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_GOOGLE_SIGN_IN) {
            try {
                GoogleSignInAccount googleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
                AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            initializeMainActivity(Provider.GOOGLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //En caso de error, se muestra un mensaje en el TextView
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getApplicationContext(), "Error: invalid credentials", Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(getApplicationContext(), "Too many requests, please try again later", Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseNetworkException) {
                            Toast.makeText(getApplicationContext(), "Network error: Could not connect to database", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e("Error - MainActivity - getSignedInAccountFromIntent", e.getMessage());
                            Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("Error - MainActivity - getSignedInAccountFromIntent", e.getMessage());
                Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeMainActivity(Provider provider) {
        //Obtiene el UID y el correo electrónico del usuario actualmente autenticado
        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        memberService.getMemberByUid(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Member m = snapshot.getChildren().iterator().next().getValue(Member.class);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", email);
                    editor.putString("role", m.getRole().name());
                    editor.putString("id", m.getId());
                    editor.apply();

                    if (m.getProvider().equals(Provider.EMAIL) && provider.equals(Provider.GOOGLE)) {
                        m.setProvider(Provider.GOOGLE);
                        memberService.updateMember(m);
                    }
                    if(m.getRole().name().equals("ADMIN")){
                        Intent adminMainIntent = new Intent(getApplicationContext(), AdminMainActivity.class);
                        startActivity(adminMainIntent);
                    }else{
                        Intent memberMainIntent = new Intent(getApplicationContext(), MemberMainActivity.class);
                        startActivity(memberMainIntent);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Signed in with Google. Finish registration to use the app.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error - MainActivity - getMemberByUid", error.getMessage());
                Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {
        if (textInputEditTextEmail.getText().toString().isBlank() || textInputEditTextPassword.getText().toString().isBlank()) {
            Toast.makeText(getApplicationContext(),"You must fill out all the fields",Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(textInputEditTextEmail.getText().toString(), textInputEditTextPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        initializeMainActivity(Provider.EMAIL);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();

                        if (errorCode.equalsIgnoreCase("ERROR_WRONG_PASSWORD") || errorCode.equalsIgnoreCase("ERROR_USER_NOT_FOUND") ||
                                errorCode.equalsIgnoreCase("ERROR_INVALID_EMAIL") || errorCode.equalsIgnoreCase("ERROR_WEAK_PASSWORD")) {
                            Toast.makeText(getApplicationContext(),"Incorrect email or password", Toast.LENGTH_LONG).show();
                        } else if (errorCode.equalsIgnoreCase("ERROR_USER_DISABLED")) {
                            Toast.makeText(getApplicationContext(),"The user is disabled", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e("Error - MainActivity - signInWithEmailAndPassword", e.getMessage());
                            Toast.makeText(getApplicationContext(),"Unexpected error", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e("Error - MainActivity - signInWithEmailAndPassword", e.getMessage());
                        Toast.makeText(getApplicationContext(),"Unexpected error", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void loadComponents() {
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        btnLogin = findViewById(R.id.btnLogin);
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        role = sharedPreferences.getString("role", "");

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();


        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);
    }

    private void loadServices(){
        memberService = new MemberService(getApplicationContext());
    }

}