package com.example.eventjoy.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventjoy.R;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.enums.Role;
import com.example.eventjoy.fragments.ProgressDialogFragment;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.MemberService;
import com.example.eventjoy.views.LockableScrollView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private LinearLayout linearLayoutSignUp;
    private LockableScrollView scrollViewSignUp;
    private ImageView ivSwipeDown, ivSwipeUp, profileIcon;
    private MotionLayout motionLayout;
    private TextView tvLogin;
    private TextInputEditText textInputEditTextName, textInputEditTextSurname, textInputEditTextUsername, textInputEditTextDni, textInputEditTextPhone, textInputEditTextBirthdate, textInputEditTextEmail, textInputEditTextPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private MemberService memberService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DateTimeFormatter formatterDate;
    private LocalDate birthdate;
    private ProgressDialogFragment progressDialog;
    private static final int PICK_IMAGE_REQUEST = 0;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

        textInputEditTextBirthdate.setOnClickListener(v -> {
            Calendar cAppointmentDate = Calendar.getInstance();
            int year = cAppointmentDate.get(Calendar.YEAR);
            int month = cAppointmentDate.get(Calendar.MONTH);
            int day = cAppointmentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dEnrollmentStart = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                textInputEditTextBirthdate.setText(formattedDate);
            }, year, month, day);
            dEnrollmentStart.show();
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifications();
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelect();
            }
        });

        motionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {
            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                if (currentId == R.id.start) {
                    linearLayoutSignUp.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.cardview));
                    ivSwipeDown.animate().alpha(0f).setDuration(200).start();
                    ivSwipeUp.setOnClickListener(v -> {
                        motionLayout.transitionToEnd();
                        ivSwipeUp.setOnClickListener(null);
                    });
                } else if (currentId == R.id.end) {
                    ivSwipeDown.animate().alpha(1f).setDuration(200).start();
                    ivSwipeDown.setOnClickListener(v -> {
                        motionLayout.transitionToStart();
                        ivSwipeDown.setOnClickListener(null);
                        scrollViewSignUp.setScrollingEnabled(false);
                    });
                    scrollViewSignUp.setScrollingEnabled(true);
                }
            }
            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
            }
        });

    }

    private void verifications() {
        if (textInputEditTextName.getText().toString().isBlank() || textInputEditTextPassword.getText().toString().isBlank() || textInputEditTextEmail.getText().toString().isBlank() || textInputEditTextUsername.getText().toString().isBlank() || textInputEditTextBirthdate.getText().toString().isBlank() || textInputEditTextDni.getText().toString().isBlank()) {
            Toast.makeText(getApplicationContext(), "You must fill out all the required fields", Toast.LENGTH_LONG).show();
        } else if (textInputEditTextName.getText().toString().length() > 20) {
            Toast.makeText(getApplicationContext(), "The name must have a maximum of 20 characters", Toast.LENGTH_LONG).show();
        } else {
            birthdate = LocalDate.parse(textInputEditTextBirthdate.getText().toString(), formatterDate);
            String formattedToday = LocalDate.now().format(formatterDate);
            LocalDate today = LocalDate.parse(formattedToday, formatterDate);

            if(birthdate.isAfter(today)){
                Toast.makeText(getApplicationContext(), "Date of birth cannot be later than the current date", Toast.LENGTH_LONG).show();
            }else if(Period.between(birthdate, today).getYears()<12){
                Toast.makeText(getApplicationContext(), "The minimum age to access the platform is 12 years old", Toast.LENGTH_LONG).show();
            }else{
                initializeDialog();

                memberService.checkRepeatedDNI(textInputEditTextDni.getText().toString(), new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String exist) {
                        if (exist == "true") {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"The DNI " + textInputEditTextDni.getText().toString() + " is already registered, try a different one", Toast.LENGTH_LONG).show();
                        } else {
                            memberService.checkRepeatedUsername(textInputEditTextUsername.getText().toString(), new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String exist) {
                                    if (exist == "true") {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(),"The username " + textInputEditTextUsername.getText().toString() + " is already registered, try a different one", Toast.LENGTH_LONG).show();
                                    } else {
                                        signUpMember();
                                    }
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void signUpMember() {

        Member m = new Member();
        m.setDni(textInputEditTextDni.getText().toString());
        m.setLevel(0);
        m.setPhone(textInputEditTextPhone.getText().toString());
        m.setUsername(textInputEditTextUsername.getText().toString());
        m.setSurname(textInputEditTextSurname.getText().toString());
        m.setProvider(Provider.EMAIL);
        m.setRole(Role.MEMBER);
        m.setBirthdate(textInputEditTextBirthdate.getText().toString());
        m.setName(textInputEditTextName.getText().toString());
        m.setPhoto(mImageUri.toString());

        mAuth.createUserWithEmailAndPassword(textInputEditTextEmail.getText().toString(), textInputEditTextPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    insertMember(m);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                if (e instanceof FirebaseAuthException) {
                    FirebaseAuthException authException = (FirebaseAuthException) e;
                    String errorCode = authException.getErrorCode();

                    switch (errorCode) {
                        case "ERROR_WEAK_PASSWORD":
                            Toast.makeText(getApplicationContext(), "The password is too weak, please choose a stronger password", Toast.LENGTH_LONG).show();
                            break;
                        case "ERROR_INVALID_EMAIL":
                            Toast.makeText(getApplicationContext(),"The email is in an invalid format, please enter a valid email", Toast.LENGTH_LONG).show();
                            break;
                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            Toast.makeText(getApplicationContext(),"The email " + textInputEditTextEmail.getText().toString() + " is already registered in another account", Toast.LENGTH_LONG).show();
                            break;
                        case "ERROR_NETWORK_REQUEST_FAILED":
                            Toast.makeText(getApplicationContext(),"A network error (such as timeout, interrupted connection or unreachable host) has occurred.", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(),"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Failed registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void permissionsCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
    }

    public void imageSelect() {
        permissionsCheck();
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Choose a photo"), PICK_IMAGE_REQUEST);
    }

    private void insertMember(Member m) {
        m.setUserAccountId(mAuth.getCurrentUser().getUid());
        memberService.insertMember(m, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String id) {
                editor.putString("email", textInputEditTextEmail.getText().toString());
                editor.putString("role", Role.MEMBER.name());
                editor.putString("id", id);
                editor.apply();
                Toast.makeText(getApplicationContext(), "Member successfully registered", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                Intent memberMainIntent = new Intent(getApplicationContext(), MemberMainActivity.class);
                startActivity(memberMainIntent);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeDialog(){
        progressDialog = new ProgressDialogFragment();
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "progressDialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    mImageUri=data.getData();
                    SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("image", String.valueOf(mImageUri));
                    editor.commit();
                    profileIcon.setImageURI(mImageUri);
                    profileIcon.invalidate();
                }
            }
        }
    }

    private void loadComponents() {
        formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mAuth = FirebaseAuth.getInstance();
        profileIcon = findViewById(R.id.profileIcon);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextBirthdate = findViewById(R.id.textInputEditTextBirthdate);
        textInputEditTextPhone = findViewById(R.id.textInputEditTextPhone);
        textInputEditTextDni = findViewById(R.id.textInputEditTextDni);
        textInputEditTextUsername = findViewById(R.id.textInputEditTextUsername);
        textInputEditTextSurname = findViewById(R.id.textInputEditTextSurname);
        textInputEditTextName = findViewById(R.id.textInputEditTextName);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        ivSwipeUp = findViewById(R.id.ivSwipeUp);
        scrollViewSignUp = findViewById(R.id.scrollViewSignUp);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        linearLayoutSignUp = findViewById(R.id.linearLayoutSignUp);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scrollViewSignUp.setScrollingEnabled(false);
        ivSwipeDown = findViewById(R.id.ivSwipeDown);
        motionLayout = findViewById(R.id.main);



        ivSwipeUp.setOnClickListener(v -> {
            motionLayout.transitionToEnd();
            ivSwipeUp.setOnClickListener(null);
        });
    }

    private void loadServices() {
        memberService = new MemberService(getApplicationContext());
    }

}