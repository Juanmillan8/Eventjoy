package com.example.eventjoy.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.eventjoy.enums.Visibility;
import com.example.eventjoy.fragments.ProgressDialogFragment;
import com.example.eventjoy.manager.CloudinaryManager;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.UserGroup;
import com.example.eventjoy.services.GroupService;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class ModifyGroupActivity extends AppCompatActivity {

    private Toolbar toolbarActivity;
    private Bundle getGroup;
    private Group group;
    private TextInputEditText textInputEditTextGroupTitle, textInputEditTextGroupDescription;
    private AutoCompleteTextView autoCompleteGroupType;
    private ImageView iconGroup, btnDeleteImage, btnCamera;
    private Button btnSaveChanges;
    private Boolean changedImage;
    private ProgressDialogFragment progressDialog;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri mImageUri;
    private GroupService groupService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modify_group);
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
                verifications();
            }
        });


        iconGroup.setOnClickListener(new View.OnClickListener() {
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
                iconGroup.setImageResource(R.drawable.default_profile_photo);
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (mImageUri != null) {
                    Picasso.get().load(mImageUri).into(iconGroup);
                    changedImage = true;
                } else {
                    Toast.makeText(this, "Error getting image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    mImageUri = data.getData();
                    iconGroup.setImageURI(mImageUri);
                    changedImage = true;
                }
            }
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialogFragment();
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "progressDialog");
    }

    private void verifications() {
        if (textInputEditTextGroupTitle.getText().toString().isBlank() || autoCompleteGroupType.getText().toString().isBlank()) {
            Toast.makeText(getApplicationContext(), "You must fill out all the required fields", Toast.LENGTH_LONG).show();
        } else {
            initializeDialog();
            group.setDescription(textInputEditTextGroupDescription.getText().toString());
            group.setVisibility(Visibility.valueOf(autoCompleteGroupType.getText().toString().toUpperCase()));
            group.setTitle(textInputEditTextGroupTitle.getText().toString());

            if (changedImage) {
                saveIconGroup(group);
            } else {
                modifyGroup(group);
            }
        }
    }

    private void saveIconGroup(Group g) {
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
                g.setIcon(imageUrl);
                modifyGroup(g);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Toast.makeText(getApplicationContext(), "Error setting group icon", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        });
    }

    private void modifyGroup(Group g) {
        groupService.updateGroup(g);
        Toast.makeText(getApplicationContext(), "Successfully modified group",Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        Intent detailsGroupIntent = new Intent(this, DetailsGroupActivity.class);
        detailsGroupIntent.putExtra("group", group);
        detailsGroupIntent.putExtra("userGroupRole", "ADMIN");
        startActivity(detailsGroupIntent);
        finish();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent detailsGroupIntent = new Intent(this, DetailsGroupActivity.class);
        detailsGroupIntent.putExtra("group", group);
        detailsGroupIntent.putExtra("userGroupRole", "ADMIN");
        startActivity(detailsGroupIntent);
        finish();
        return true;
    }

    private void loadServices(){
        groupService = new GroupService(getApplicationContext());
    }

    private void loadComponents(){
        changedImage=false;
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        btnCamera = findViewById(R.id.btnCamera);
        toolbarActivity = findViewById(R.id.toolbarActivity);
        iconGroup = findViewById(R.id.iconGroup);
        setSupportActionBar(toolbarActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getGroup = getIntent().getExtras();
        group = (Group) getGroup.getSerializable("group");

        textInputEditTextGroupTitle = findViewById(R.id.textInputEditTextGroupTitle);
        textInputEditTextGroupDescription = findViewById(R.id.textInputEditTextGroupDescription);
        autoCompleteGroupType = findViewById(R.id.autoCompleteGroupType);

        String[] groupTypes = {"Public", "Private"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupTypes);
        autoCompleteGroupType.setAdapter(adapter);

        textInputEditTextGroupTitle.setText(group.getTitle());
        textInputEditTextGroupDescription.setText(group.getDescription());

        if (group.getIcon() != null && !group.getIcon().isEmpty()) {
            changedImage=true;
            Picasso.get().load(group.getIcon()).into(iconGroup);
        }

        if(group.getVisibility().name().equals("PUBLIC")){
            autoCompleteGroupType.setText("Public", false);
        }else{
            autoCompleteGroupType.setText("Private", false);
        }

    }

}