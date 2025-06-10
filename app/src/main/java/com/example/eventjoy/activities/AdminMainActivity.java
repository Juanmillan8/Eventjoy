package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventjoy.R;
import com.example.eventjoy.databinding.ActivityAdminMainBinding;
import com.example.eventjoy.databinding.ActivityMemberMainBinding;
import com.example.eventjoy.fragments.ProgressDialogFragment;
import com.example.eventjoy.models.Admin;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.AdminService;
import com.example.eventjoy.services.MemberService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

public class AdminMainActivity extends AppCompatActivity {
    //TODO CONTROLAR LOS TOAST PARA QUE SE ME MUESTREN BIEN Y NO SE MUESTREN CORTADO
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityAdminMainBinding binding;
    private NavigationView navigationView;
    private ProgressDialogFragment progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private SharedPreferences sharedPreferences;
    private TextView tvName, tvEmail;
    private ImageView profileIcon;
    private AdminService adminService;
    private Admin admin;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarAdminMain.toolbarAdmin);
        DrawerLayout drawer = binding.drawerLayoutAdmin;
        navigationView = binding.navViewAdmin;

        loadServices();
        loadComponents();

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.homeAdminFragment, R.id.logout).setOpenableLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_admin_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayoutAdmin.closeDrawers();
            if (item.getItemId() == R.id.homeAdminFragment) {
                navController.navigate(R.id.homeAdminFragment);
                return true;
            }else if (item.getItemId() == R.id.logout) {
                logout();
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
        });
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialogFragment();
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "progressDialog");
    }

    private void loadComponents() {
        initializeDialog();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        tvName = navigationView.getHeaderView(0).findViewById(R.id.tvName);
        tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvEmail);
        profileIcon = navigationView.getHeaderView(0).findViewById(R.id.profileIcon);
        Log.i("IDE", sharedPreferences.getString("id", ""));
        adminService.getAdminById(sharedPreferences.getString("id", ""), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                admin = dataSnapshot.getChildren().iterator().next().getValue(Admin.class);
                tvName.setText("Hello, " + admin.getName());
                tvEmail.setText(user.getEmail());
                if (admin.getPhoto() != null && !admin.getPhoto().isEmpty()) {
                    Picasso.get().load(admin.getPhoto()).into(profileIcon);
                }
                progressDialog.dismissAllowingStateLoss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error - AdminMainActivity - getAdminById", databaseError.getMessage());
                Toast.makeText(getApplicationContext(), "Error querying database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServices() {
        adminService = new AdminService(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin_main, menu);
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //No se realiza ninguna acción, ya que el usuario está actualmente en su actividad principal, por lo que no puede volver atrás a menos que cierre
        //sesión y vuelva a la MainActivity
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        }
        return true;
    }

    private void logout() {
        editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        mAuth.signOut();

        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_admin_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}