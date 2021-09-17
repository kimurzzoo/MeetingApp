package com.example.meetingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Observable;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ExtraProfileActivity extends AppCompatActivity {

    private CheckBox maleCheckbox;
    private CheckBox femaleCheckbox;
    private EditText ageText;
    private EditText jobText;
    private EditText memoText;
    private FirebaseAuth mAuth;
    private Activity activity;
    private Button nextbutton;
    private TextView locationtext;
    private Button locationbutton;
    private String[] locations = {"서울", "인천", "대전", "광주", "대구", "울산", "부산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"};
    private AlertDialog locationDialog;
    private TextView loadingtext;
    private FirebaseDatabase database;

    public ExtraProfileActivity()
    {

    }

    public ExtraProfileActivity(Activity context) {this.activity = context;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_profile);

        database = FirebaseDatabase.getInstance();

        maleCheckbox = (CheckBox) findViewById(R.id.malecheckbox);
        femaleCheckbox = (CheckBox) findViewById(R.id.femalecheckbox);
        ageText = (EditText) findViewById(R.id.agetext);
        jobText = (EditText) findViewById(R.id.jobtext);
        memoText = (EditText) findViewById(R.id.memotext);
        nextbutton = (Button) findViewById(R.id.nextbutton);
        locationbutton = (Button) findViewById(R.id.locationbutton);
        locationtext = (TextView) findViewById(R.id.locationtext);
        loadingtext = (TextView) findViewById(R.id.loadingtextextra);

        mAuth = FirebaseAuth.getInstance();

        maleCheckbox.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    femaleCheckbox.setChecked(false);
                }
            }
        });

        femaleCheckbox.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    maleCheckbox.setChecked(false);
                }
            }
        });

        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!maleCheckbox.isChecked() && !femaleCheckbox.isChecked())
                {
                    toastMachine("성별을 선택해주세요.");
                }
                else if(ageText.getText().toString().equals(""))
                {
                    toastMachine("나이를 입력해주세요.");
                }
                else if(locationtext.getText().toString().equals("선택"))
                {
                    toastMachine("지역을 선택해주세요.");
                }
                else if(jobText.getText().toString().equals(""))
                {
                    toastMachine("직업 혹은 학력을 입력해주세요.");
                }
                else if(memoText.getText().toString().equals(""))
                {
                    toastMachine("자기소개를 입력해주세요.");
                }
                else
                {
                    updateUI(mAuth.getCurrentUser());
                }
            }
        });

        locationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationDialog.show();
            }
        });

        locationDialog = new AlertDialog.Builder(ExtraProfileActivity.this).setItems(locations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                locationtext.setText(locations[i]);
            }
        }).setTitle("지역 선택").setPositiveButton("확인", null).setNegativeButton("취소", null).create();
    }

    private void updateUI(FirebaseUser user) { //update ui code here
        loadingtext.setText("로딩중...");
        if (user != null) {
            if(user.isEmailVerified())
            {
                String userId = user.getUid();
                Log.d("kimurzzoo", "user id : " + userId);

                if(database == null)
                {
                    Log.d("kimurzzoo", "db is null");
                }
                DatabaseReference userRef = database.getReference().child("profile").child(userId);
                DatabaseReference indexRef = database.getReference().child("index");

                DatabaseReference sexRef;
                DatabaseReference ageRef;
                DatabaseReference finalIndexRef;

                int sextemp = - 1;
                sexRef = indexRef.child("sex");

                if(maleCheckbox.isChecked())
                {
                    sexRef = indexRef.child("male");
                    sextemp = 1;
                }
                else if(femaleCheckbox.isChecked())
                {
                    sexRef = indexRef.child("female");
                    sextemp = 0;
                }
                else
                {
                    toastMachine("성별이 없습니다.");
                    return;
                }

                int agetemp = Integer.parseInt(ageText.getText().toString());
                ageRef = sexRef.child(Integer.toString(agetemp));
                finalIndexRef = ageRef.child(locationtext.getText().toString());

                String emailtemp = user.getEmail();
                String nametemp = user.getDisplayName();

                int ranint;
                Random random = new Random();
                String authnumber = "";

                for(int i=0; i<7; i++)
                {
                    ranint = random.nextInt(9);
                    authnumber = authnumber + Integer.toString(ranint);
                }

                UserProfile userProfile = new UserProfile(agetemp, authnumber, false, emailtemp, jobText.getText().toString(), locationtext.getText().toString(), memoText.getText().toString(), nametemp, sextemp, false, true, true);
                userRef.setValue(userProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finalIndexRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                finalIndexRef.child("number").runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData1) {
                                        if(snapshot.getValue() == null && currentData1.getValue() == null)
                                        {
                                            currentData1.setValue(1);
                                            finalIndexRef.child("user").child(userId).setValue(userId);
                                        }
                                        else if(snapshot.getValue() != null && currentData1.getValue() == null)
                                        {
                                            return Transaction.success(currentData1);
                                        }
                                        else
                                        {
                                            if(snapshot.getValue() == null && currentData1.getValue(Integer.class) > 0)
                                            {
                                                currentData1.setValue(1);
                                            }
                                            else
                                            {
                                                currentData1.setValue(currentData1.getValue(Integer.class) + 1);
                                            }
                                            finalIndexRef.child("user").child(userId).setValue(userId);
                                        }
                                        return Transaction.success(currentData1);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData1) {
                                        if(snapshot.getValue() != null && currentData1.getValue() == null)
                                        {
                                            return;
                                        }
                                        DatabaseReference userRemainRef = database.getReference().child("userremain").child(userId);
                                        UserRemain userRemain = new UserRemain(3, 20);
                                        userRemainRef.setValue(userRemain).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(ExtraProfileActivity.this, PictureSelectActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });

                Log.d("kimurzzoo", "db send complete");
            }
            else
            {
                loadingtext.setText("로딩 실패");
                toastMachine("이메일이 인증되지 않았습니다.");
            }
        }
        else
        {
            toastMachine("로그인에 실패하였습니다.");
        }
    }

    private void toastMachine(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}