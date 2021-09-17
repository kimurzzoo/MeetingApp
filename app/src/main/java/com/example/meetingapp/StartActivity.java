package com.example.meetingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            if(mAuth.getCurrentUser().isEmailVerified())
            {
                String userId = mAuth.getCurrentUser().getUid();

                FirebaseDatabase database = FirebaseDatabase.getInstance();

                DatabaseReference profileRef = database.getReference().child("profile").child(userId);

                profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if(userProfile.extraprofiletutorial)
                        {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(StartActivity.this, ExtraProfileActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1500);
                        }
                        else if(userProfile.pictureselecttutorial)
                        {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(StartActivity.this, PictureSelectActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1500);
                        }
                        else if(userProfile.introductiontutorial)
                        {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(StartActivity.this, IntroductionActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1500);
                        }
                        else
                        {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1500);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else
            {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(StartActivity.this, TutorialActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 1500);
            }
        }
        else
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(StartActivity.this, TutorialActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1500);
        }
    }
}
