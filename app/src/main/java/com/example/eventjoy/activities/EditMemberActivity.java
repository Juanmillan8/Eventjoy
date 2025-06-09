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
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.eventjoy.fragments.ProgressDialogFragment;
import com.example.eventjoy.manager.CloudinaryManager;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.MemberService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;

public class EditMemberActivity extends AppCompatActivity {
    private Bundle getMember;
    private Member memberEdit;
    private TextInputEditText textInputEditTextName, textInputEditTextSurname, textInputEditTextUsername, textInputEditTextDni, textInputEditTextPhone, textInputEditTextBirthdate, textInputEditTextEmail;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private ImageView profileIcon, btnDeleteImage, btnCamera;
    private Toolbar toolbarActivity;
    private Button btnSaveChanges;
    private ProgressDialogFragment progressDialog;
    private MemberService memberService;
    private LocalDate birthdate;
    private DateTimeFormatter inputFormatter, outputFormatter;
    private static final int PICK_IMAGE_REQUEST = 0;
    private Uri mImageUri;
    private Boolean changedImage, defaultImage;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_member);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadServices();
        loadComponents();

        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                }
                verifications();
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelect();
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

        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changedImage = false;
                defaultImage = false;
                profileIcon.setImageResource(R.drawable.default_profile_photo);
            }
        });

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

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (mImageUri != null) {
                    Picasso.get().load(mImageUri).into(profileIcon);
                    changedImage = true;
                    defaultImage = false;
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

    private File createImageFile() throws IOException {
        String timeStamp = LocalDateTime.now().toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
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

    public void permissionsCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
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
                    defaultImage = false;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return goBack();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return goBack();
    }

    private boolean goBack() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            Toast.makeText(getApplicationContext(), "Your session has expired", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
        return true;
    }

    private void verifications() {
        if (textInputEditTextName.getText().toString().isBlank() || textInputEditTextEmail.getText().toString().isBlank() || textInputEditTextUsername.getText().toString().isBlank() || textInputEditTextBirthdate.getText().toString().isBlank() || textInputEditTextDni.getText().toString().isBlank()) {
            Toast.makeText(getApplicationContext(), "You must fill out all the required fields", Toast.LENGTH_LONG).show();
        } else if (textInputEditTextName.getText().toString().length() > 20) {
            Toast.makeText(getApplicationContext(), "The name must have a maximum of 20 characters", Toast.LENGTH_LONG).show();
        } else {
            LocalDate date = LocalDate.parse(textInputEditTextBirthdate.getText().toString(), inputFormatter);
            String formattedDate = outputFormatter.format(date);
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
                        if (!textInputEditTextDni.getText().toString().equals(memberEdit.getDni().toString()) && snapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "The DNI " + textInputEditTextDni.getText().toString() + " is already registered, try a different one", Toast.LENGTH_LONG).show();
                        } else {
                            memberService.checkRepeatedUsername(textInputEditTextUsername.getText().toString(), new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!textInputEditTextUsername.getText().toString().equals(memberEdit.getUsername().toString()) && snapshot.exists()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "The username " + textInputEditTextUsername.getText().toString() + " is already registered, try a different one", Toast.LENGTH_LONG).show();
                                    } else {
                                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                            saveProfileImage();
                                        } else {
                                            Intent showPoPup = new Intent(getApplicationContext(), PopupReauthenticateActivity.class);
                                            startActivity(showPoPup);
                                            progressDialog.dismiss();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    //Se cierra la ventana de carga
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Error querying database " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //Se cierra la ventana de carga
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error querying database " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialogFragment();
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "progressDialog");
    }

    private void saveProfileImage() {
        if (changedImage) {
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
                    memberEdit.setPhoto(imageUrl);
                    editMember();
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error saving profile picture: " + error.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                }
            });
        } else {
            if(defaultImage){
                editMember();
            }else{
                memberEdit.setPhoto(null);
                editMember();
            }


        }
    }

    private void editMember() {
        memberEdit.setDni(textInputEditTextDni.getText().toString());
        memberEdit.setPhone(textInputEditTextPhone.getText().toString());
        memberEdit.setUsername(textInputEditTextUsername.getText().toString());
        memberEdit.setSurname(textInputEditTextSurname.getText().toString());
        memberEdit.setBirthdate(birthdate.toString());
        memberEdit.setName(textInputEditTextName.getText().toString());

        user.updateEmail(textInputEditTextEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    memberService.updateMember(memberEdit);
                    Toast.makeText(getApplicationContext(), "Successfully edited member", Toast.LENGTH_SHORT).show();
                    editor = sharedPreferences.edit();
                    editor.putString("email", textInputEditTextEmail.getText().toString());
                    editor.apply();
                    Intent mainMemberIntent = new Intent(getApplicationContext(), MemberMainActivity.class);
                    startActivity(mainMemberIntent);
                    finish();
                    progressDialog.dismiss();
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
                        case "ERROR_USER_DISABLED":
                            Toast.makeText(getApplicationContext(), "The user is disabled", Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR_REQUIRES_RECENT_LOGIN":
                            Toast.makeText(getApplicationContext(), "HAY QUE REAUTENTICAR AL USUARIO", Toast.LENGTH_SHORT).show();
                            Intent showPoPup = new Intent(getApplicationContext(), PopupReauthenticateActivity.class);
                            startActivity(showPoPup);
                            break;
                        case "ERROR_INVALID_EMAIL":
                            Toast.makeText(getApplicationContext(), "The email is in an invalid format, please enter a valid email", Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            Toast.makeText(getApplicationContext(), "The email " + textInputEditTextEmail.getText().toString() + " is already registered in another account", Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR_NETWORK_REQUEST_FAILED":
                            Toast.makeText(getApplicationContext(), "A network error (such as timeout, interrupted connection or unreachable host) has occurred.", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadComponents() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        changedImage = false;
        defaultImage = false;
        inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileIcon = findViewById(R.id.profileIcon);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        btnCamera = findViewById(R.id.btnCamera);
        textInputEditTextName = findViewById(R.id.textInputEditTextName);
        textInputEditTextSurname = findViewById(R.id.textInputEditTextSurname);
        textInputEditTextUsername = findViewById(R.id.textInputEditTextUsername);
        textInputEditTextDni = findViewById(R.id.textInputEditTextDni);
        textInputEditTextPhone = findViewById(R.id.textInputEditTextPhone);
        textInputEditTextBirthdate = findViewById(R.id.textInputEditTextBirthdate);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        getMember = getIntent().getExtras();
        memberEdit = (Member) getMember.getSerializable("member");
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);

        textInputEditTextName.setText(memberEdit.getName());
        textInputEditTextSurname.setText(memberEdit.getSurname());
        textInputEditTextUsername.setText(memberEdit.getUsername());
        textInputEditTextDni.setText(memberEdit.getDni());
        textInputEditTextPhone.setText(memberEdit.getPhone());


        LocalDate birthDate = LocalDate.parse(memberEdit.getBirthdate(), outputFormatter);


        textInputEditTextBirthdate.setText(inputFormatter.format(birthDate));


        textInputEditTextEmail.setText(sharedPreferences.getString("email", ""));
        if (memberEdit.getPhoto() != null && !memberEdit.getPhoto().isEmpty()) {
            Picasso.get().load(memberEdit.getPhoto()).into(profileIcon);
            defaultImage = true;
        }

        if (memberEdit.getProvider().equals(Provider.GOOGLE)) {
            textInputEditTextEmail.setEnabled(false);
        }
    }

    private void loadServices() {
        memberService = new MemberService(getApplicationContext());
    }
}