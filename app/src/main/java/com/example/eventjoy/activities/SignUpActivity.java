package com.example.eventjoy.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.eventjoy.R;
import com.example.eventjoy.enums.Provider;
import com.example.eventjoy.enums.Role;
import com.example.eventjoy.fragments.ProgressDialogFragment;
import com.example.eventjoy.manager.CloudinaryManager;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private LinearLayout linearLayoutSignUp;
    private LockableScrollView scrollViewSignUp;
    private ImageView ivSwipeDown, ivSwipeUp, profileIcon, btnDeleteImage, btnCamera;
    private MotionLayout motionLayout;
    private TextView tvLogin, tvPassword;
    private TextInputEditText textInputEditTextName, textInputEditTextSurname, textInputEditTextUsername, textInputEditTextDni, textInputEditTextPhone, textInputEditTextBirthdate, textInputEditTextEmail, textInputEditTextPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private MemberService memberService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LocalDate birthdate;
    private ProgressDialogFragment progressDialog;
    private static final int PICK_IMAGE_REQUEST = 0;
    private Uri mImageUri;
    private FirebaseUser user;
    private TextInputLayout textInputLayoutPassword;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Boolean changedImage;
    private DateTimeFormatter inputFormatter, outputFormatter;


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

        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changedImage = false;
                profileIcon.setImageResource(R.drawable.default_profile_photo);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifications();
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
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

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (mImageUri != null) {
                    Picasso.get().load(mImageUri).into(profileIcon);
                    changedImage = true;
                } else {
                    Toast.makeText(this, "Error getting image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    mImageUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    cameraLauncher.launch(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Error: ", e.getMessage());
                Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void verifications() {
        if (textInputEditTextName.getText().toString().isBlank() || (textInputEditTextPassword.getText().toString().isBlank() && textInputLayoutPassword.getVisibility() == View.VISIBLE) || textInputEditTextEmail.getText().toString().isBlank() || textInputEditTextUsername.getText().toString().isBlank() || textInputEditTextBirthdate.getText().toString().isBlank() || textInputEditTextDni.getText().toString().isBlank()) {
            Toast.makeText(getApplicationContext(), "You must fill out all the required fields", Toast.LENGTH_LONG).show();
        } else if (textInputEditTextName.getText().toString().length() > 20) {
            Toast.makeText(getApplicationContext(), "The name must have a maximum of 20 characters", Toast.LENGTH_LONG).show();
        } else {
            LocalDate date = LocalDate.parse(textInputEditTextBirthdate.getText().toString(), inputFormatter);
            String formattedDate = date.format(outputFormatter);

            birthdate = LocalDate.parse(formattedDate);
            LocalDate today = LocalDate.now();

            if (birthdate.isAfter(today)) {
                Toast.makeText(getApplicationContext(), "Date of birth cannot be later than the current date", Toast.LENGTH_LONG).show();
            } else if (Period.between(birthdate, today).getYears() < 12) {
                Toast.makeText(getApplicationContext(), "The minimum age to access the platform is 12 years old", Toast.LENGTH_LONG).show();
            } else {
                initializeDialog();
                memberService.checkRepeatedDNI(textInputEditTextDni.getText().toString(), new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "The DNI is already registered, try a different one", Toast.LENGTH_LONG).show();
                        } else {
                            memberService.checkRepeatedUsername(textInputEditTextUsername.getText().toString(), new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "The username is already registered, try a different one", Toast.LENGTH_LONG).show();
                                    } else {
                                        signUpMember();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    //Se cierra la ventana de carga
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //Se cierra la ventana de carga
                        progressDialog.dismiss();
                        Log.e("Error - SignUpActivity - checkRepeatedDNI", error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error querying database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        m.setRole(Role.MEMBER);
        m.setBirthdate(birthdate.toString());
        m.setName(textInputEditTextName.getText().toString());

        if (user == null) {
            mAuth.createUserWithEmailAndPassword(textInputEditTextEmail.getText().toString(), textInputEditTextPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        m.setProvider(Provider.EMAIL);
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
                                Toast.makeText(getApplicationContext(), "The password is too weak", Toast.LENGTH_LONG).show();
                                break;
                            case "ERROR_INVALID_EMAIL":
                                Toast.makeText(getApplicationContext(), "The email is in an invalid format", Toast.LENGTH_LONG).show();
                                break;
                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                Toast.makeText(getApplicationContext(), "The email is already registered in another account", Toast.LENGTH_LONG).show();
                                break;
                            case "ERROR_NETWORK_REQUEST_FAILED":
                                Toast.makeText(getApplicationContext(), "A network error has occurred.", Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Log.e("Error - SignUpActivity - createUserWithEmailAndPassword", e.getMessage());
                                Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Error - SignUpActivity - createUserWithEmailAndPassword", e.getMessage());
                        Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            m.setProvider(Provider.GOOGLE);
            insertMember(m);
        }
    }

    public void permissionsCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
        String idMember = memberService.insertMember(m);

        editor.putString("email", textInputEditTextEmail.getText().toString());
        editor.putString("role", Role.MEMBER.name());
        editor.putString("id", idMember);
        editor.apply();

        if (changedImage) {
            saveProfileImage(m);
        } else {
            Toast.makeText(getApplicationContext(), "Member successfully registered", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            Intent memberMainIntent = new Intent(getApplicationContext(), MemberMainActivity.class);
            startActivity(memberMainIntent);
        }
    }

    private void saveProfileImage(Member m) {
        CloudinaryManager.uploadImage(getApplicationContext(), mImageUri, new UploadCallback() {
            @Override
            public void onStart(String requestId) {
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String imageUrl = resultData.get("secure_url").toString();
                m.setPhoto(imageUrl);
                memberService.updateMember(m);
                Toast.makeText(getApplicationContext(), "Member successfully registered", Toast.LENGTH_SHORT).show();
                Intent memberMainIntent = new Intent(getApplicationContext(), MemberMainActivity.class);
                startActivity(memberMainIntent);
                progressDialog.dismiss();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Toast.makeText(getApplicationContext(), "Registered member without profile photo", Toast.LENGTH_SHORT).show();
                Intent memberMainIntent = new Intent(getApplicationContext(), MemberMainActivity.class);
                startActivity(memberMainIntent);
                progressDialog.dismiss();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = LocalDateTime.now().toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialogFragment();
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "progressDialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_sign_out_menu, menu);
        if (user != null) {
            MenuItem actionIcon = menu.findItem(R.id.logout_icon);
            actionIcon.setVisible(true);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (user != null) {
                Toast.makeText(getApplicationContext(), "You must log out before returning to the main screen", Toast.LENGTH_SHORT).show();
            } else {
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                startActivity(mainActivityIntent);
            }
        } else {
            mAuth.signOut();
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (user != null) {
                Toast.makeText(getApplicationContext(), "You must log out before returning to the main screen", Toast.LENGTH_SHORT).show();
            } else {
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                startActivity(mainActivityIntent);
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    mImageUri = data.getData();
                    profileIcon.setImageURI(mImageUri);
                    changedImage = true;
                }
            }
        }
    }

    private void loadComponents() {
        outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        changedImage = false;
        tvPassword = findViewById(R.id.tvPassword);
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mAuth = FirebaseAuth.getInstance();
        profileIcon = findViewById(R.id.profileIcon);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextBirthdate = findViewById(R.id.textInputEditTextBirthdate);
        textInputEditTextPhone = findViewById(R.id.textInputEditTextPhone);
        textInputEditTextDni = findViewById(R.id.textInputEditTextDni);
        btnCamera = findViewById(R.id.btnCamera);
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
        user = mAuth.getCurrentUser();
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);


        ivSwipeUp.setOnClickListener(v -> {
            motionLayout.transitionToEnd();
            ivSwipeUp.setOnClickListener(null);
        });


        if (user != null) {
            textInputEditTextEmail.setText(user.getEmail());
            textInputEditTextEmail.setEnabled(false);
            textInputEditTextName.setText(user.getDisplayName());
            textInputEditTextPhone.setText(user.getPhoneNumber());
            textInputLayoutPassword.setVisibility(View.GONE);
            tvPassword.setVisibility(View.GONE);

            if (user.getPhotoUrl() != null) {
                Picasso.get().load(user.getPhotoUrl()).into(profileIcon);
                changedImage = true;

                // Guardar imagen en archivo temporal usando Picasso (esto lo hace en segundo plano)
                Picasso.get().load(user.getPhotoUrl()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        try {
                            File file = createImageFile();
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();

                            mImageUri = FileProvider.getUriForFile(SignUpActivity.this, getPackageName(), file);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("ImageSaveError", "Could not save image: " + e.getMessage());
                            Toast.makeText(getApplicationContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.e("Picasso", "Error loading image: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            }
        }
    }

    private void loadServices() {
        memberService = new MemberService(getApplicationContext());
    }

}