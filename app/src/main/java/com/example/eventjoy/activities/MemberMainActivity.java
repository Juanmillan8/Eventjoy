package com.example.eventjoy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.eventjoy.R;
import com.example.eventjoy.databinding.ActivityMemberMainBinding;
import com.example.eventjoy.fragments.ProgressDialogFragment;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.services.MemberService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import java.io.File;

public class MemberMainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMemberMainBinding binding;
    private ProgressDialogFragment progressDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView tvName, tvEmail;
    private ImageView profileIcon;
    private NavigationView navigationView;
    private MemberService memberService;
    private Bundle bundle;
    private Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMemberMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMemberMain.toolbarMember);
        DrawerLayout drawer = binding.drawerLayoutMember;
        navigationView = binding.navViewMember;

        loadServices();
        loadComponents();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeMemberFragment, R.id.detailsMemberFragment)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_member_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayoutMember.closeDrawers();
            if (item.getItemId() == R.id.logout) {
                logout();
                return true;
            } else if (item.getItemId() == R.id.detailsMemberFragment) {
                bundle = new Bundle();
                bundle.putSerializable("member", member);
                navController.navigate(R.id.detailsMemberFragment, bundle);
                return true;
            } else if (item.getItemId() == R.id.homeMemberFragment) {
                navController.navigate(R.id.homeMemberFragment);
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.member_main, menu);
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_member_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialogFragment();
        progressDialog.setCancelable(false);
        progressDialog.show(getSupportFragmentManager(), "progressDialog");
    }

    private void loadProfileIcon(String filename) {
        profileIcon=null;
        profileIcon = navigationView.getHeaderView(0).findViewById(R.id.profileIcon);
        File directory = getApplicationContext().getFilesDir();
        File imageFile = new File(directory, filename);
        Picasso.get().load(imageFile).into(profileIcon);
    }

    private void loadComponents() {
        Log.i("1", "1");
        initializeDialog();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("EventjoyPreferences", Context.MODE_PRIVATE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        tvName = navigationView.getHeaderView(0).findViewById(R.id.tvName);
        tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvEmail);
        profileIcon = navigationView.getHeaderView(0).findViewById(R.id.profileIcon);

        memberService.getMemberById(sharedPreferences.getString("id", ""), new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshot) {
                member = snapshot.getDocuments().get(0).toObject(Member.class);
                Log.i("2", "2");
                tvName.setText("Hello, " + member.getName());
                tvEmail.setText(user.getEmail());
                Log.i("getfotoooooooo", member.getPhoto());
                if (member.getPhoto() != null) {
                    Log.i("3", "3");
                    loadProfileIcon(member.getPhoto());
                }
                Log.i("4", "4");
                progressDialog.dismiss();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error querying database " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServices() {
        memberService = new MemberService(getApplicationContext());
    }
}