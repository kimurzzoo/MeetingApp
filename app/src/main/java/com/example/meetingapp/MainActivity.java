package com.example.meetingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private UserProfile userProfile;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private RequestFragment requestFragment;
    private MessageFragment messageFragment;
    private AcceptFragment acceptFragment;
    private FirebaseUser user;
    private ImageView user_picture;
    private TextView username;
    private TextView jobtext;
    private TextView toolbar_name;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ForcedTerminationService.class));


        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar_name = findViewById(R.id.toolbar_name);

        requestFragment = new RequestFragment();
        messageFragment = new MessageFragment();
        acceptFragment = new AcceptFragment();

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_24px);

        LinearLayout navigation_container = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.navigation_profile, null);
        navigation_container.setBackgroundColor(Color.parseColor("#ADA49B"));
        navigation_container.setOrientation(LinearLayout.VERTICAL);
        navigation_container.setGravity(Gravity.BOTTOM);
        navigation_container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        user_picture = new ImageView(this);
        user_picture.setAdjustViewBounds(true);
        user_picture.setMaxHeight(600);
        user_picture.setMaxWidth(600);
        username = new TextView(this);
        jobtext = new TextView(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        loadProfile();

        navigation_container.addView(user_picture);
        navigation_container.addView(username);
        navigation_container.addView(jobtext);

        navigationView.addHeaderView(navigation_container);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                drawerLayout.closeDrawers();

                int id = item.getItemId();

                if(id == R.id.side_profile)
                {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivityForResult(intent, 110);
                }
                else if(id == R.id.side_setting)
                {

                }
                else if(id == R.id.side_report)
                {

                }
                return true;
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int item_id = item.getItemId();
                bottomNavigateChangeFragment(item_id);
                return true;
            }
        });

        getSupportFragmentManager().beginTransaction().add(R.id.main_frame,requestFragment).commit();
        toolbar_name.setText("인연 신청");
    }

    private void bottomNavigateChangeFragment(int id)
    {
        switch(id)
        {
            case R.id.send_request:
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,requestFragment).commit();
                toolbar_name.setText("인연 신청");
                break;
            }
            case R.id.message_tab:
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,messageFragment).commit();
                toolbar_name.setText("메시지");
                break;
            }
            case R.id.accept_request:
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,acceptFragment).commit();
                toolbar_name.setText("받은 신청");
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toastMachine(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isOpen())
        {
            drawerLayout.closeDrawers();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 110)
        {
            loadProfile();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadProfile()
    {
        if(user != null)
        {
            if(user.isEmailVerified())
            {
                String uid = user.getUid();
                database = FirebaseDatabase.getInstance();
                DatabaseReference profileRef = database.getReference().child("profile").child(uid);
                profileRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userProfile = snapshot.getValue(UserProfile.class);
                        username.setText(userProfile.getName());
                        jobtext.setText(userProfile.getJob());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                storage = FirebaseStorage.getInstance();
                final StorageReference storageRef = storage.getReference();
                final StorageReference profileStorage = storageRef.child(uid + "/picture/profile.jpg");

                Glide.with(this).load(profileStorage).into(user_picture);
            }
        }
    }
}