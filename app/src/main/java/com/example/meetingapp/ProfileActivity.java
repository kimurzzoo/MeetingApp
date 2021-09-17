package com.example.meetingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.loader.content.CursorLoader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profile_image;
    private Button profile_image_modify_button;
    private CheckBox profile_malecheckbox;
    private CheckBox profile_femalecheckbox;
    private EditText profile_age_text;
    private TextView profile_locationtext;
    private Button profile_locationbutton;
    private EditText profile_job_text;
    private EditText profile_memo_text;
    private VideoView profile_intro_link;
    private Button profile_intro_modify_button;
    private Button profile_save_button;
    private TextView profile_code;
    private AlertDialog locationDialog;
    private String[] locations = {"서울", "인천", "대전", "광주", "대구", "울산", "부산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"};
    private Uri selectedImageUri;
    private Uri videoUri;
    private boolean modifiedimage = false;
    private boolean modifiedvideo = false;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String userId;
    private File localVideoFile = null;
    private TextView profile_loading_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        startService(new Intent(this, ForcedTerminationService.class));

        profile_image = (ImageView) findViewById(R.id.profile_image);
        profile_image_modify_button = (Button) findViewById(R.id.profile_image_modify_button);
        profile_malecheckbox = (CheckBox) findViewById(R.id.profile_malecheckbox);
        profile_femalecheckbox = (CheckBox) findViewById(R.id.profile_femalecheckbox);
        profile_age_text = (EditText) findViewById(R.id.profile_age_text);
        profile_locationtext = (TextView) findViewById(R.id.profile_locationtext);
        profile_locationbutton = (Button) findViewById(R.id.profile_locationbutton);
        profile_job_text = (EditText) findViewById(R.id.profile_job_text);
        profile_memo_text = (EditText) findViewById(R.id.profile_memo_text);
        profile_intro_link = (VideoView) findViewById(R.id.profile_intro_link);
        profile_intro_modify_button = (Button) findViewById(R.id.profile_intro_modify_button);
        profile_save_button = (Button) findViewById(R.id.profile_save_button);
        profile_code = (TextView) findViewById(R.id.profile_code);
        profile_loading_text = (TextView) findViewById(R.id.profile_loading_text);

        setSupportActionBar((Toolbar) findViewById(R.id.profile_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_24px);

        extraProfileDownload();

        profile_intro_link.setMediaController(new MediaController(this));

        profile_malecheckbox.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    profile_femalecheckbox.setChecked(false);
                }
            }
        });

        profile_femalecheckbox.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    profile_malecheckbox.setChecked(false);
                }
            }
        });

        locationDialog = new AlertDialog.Builder(ProfileActivity.this).setItems(locations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                profile_locationtext.setText(locations[i]);
            }
        }).setTitle("지역 선택").setPositiveButton("확인", null).setNegativeButton("취소", null).create();

        profile_locationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationDialog.show();
            }
        });

        profile_image_modify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/");
                startActivityForResult(intent, 200);
            }
        });

        profile_intro_modify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, 300);
            }
        });

        profile_save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                extraProfileUpload();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profile_image.setImageURI(selectedImageUri);
            modifiedimage = true;
        }
        else if(requestCode == 300 && resultCode == RESULT_OK)
        {
            videoUri = data.getData();
            Cursor returnCursor = getContentResolver().query(videoUri, null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            if(returnCursor.getLong(sizeIndex) > 50 * 1024 * 1024)
            {
                toastMachine("용량이 너무 큽니다.");
                videoUri = null;
            }
            else
            {
                profile_intro_link.setVideoURI(videoUri);
                modifiedvideo = true;
            }
        }
    }

    private void extraProfileDownload()
    {
        //final TaskCompletionSource<DataSnapshot> tcs = new TaskCompletionSource<>();
        profile_loading_text.setText("로딩 중...");
        DatabaseReference profileRef = database.getReference().child("profile").child(userId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //tcs.setResult(snapshot);
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                if(userProfile.getSex() == 1)
                {
                    profile_malecheckbox.setChecked(true);
                }
                else
                {
                    profile_femalecheckbox.setChecked(true);
                }

                profile_age_text.setText(Integer.toString(userProfile.getAge()));
                profile_locationtext.setText(userProfile.location);
                profile_job_text.setText(userProfile.getJob());
                profile_memo_text.setText(userProfile.getMemo());
                profile_code.setText(userProfile.authnumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profile_loading_text.setText("로딩 실패");
            }
        });

        /*Task<DataSnapshot> t;

        t = tcs.getTask();

        try
        {
            Tasks.await(t);
        }
        catch (ExecutionException | InterruptedException e)
        {
            t = Tasks.forException(e);
        }

        if(!t.isSuccessful()) {
            profile_loading_text.setText("로딩 실패");
            return;
        }*/

        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(userId + "/picture/profile.jpg");
        StorageReference videoRef = storageRef.child(userId + "/video/intro.mp4");

        try
        {
            localVideoFile = File.createTempFile("user_video", ".mp4", getExternalFilesDir(Environment.DIRECTORY_MOVIES));

            videoRef.getFile(localVideoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d("kimurzzoo","video download complete");
                    profile_intro_link.setVideoURI(Uri.fromFile(localVideoFile));
                    profile_intro_link.start();
                }
            });
            Glide.with(this).load(imageRef).into(profile_image);
            profile_loading_text.setText("로딩 완료!");
        }
        catch(IOException e)
        {
            profile_loading_text.setText("로딩 실패");
        }


    }

    private void extraProfileUpload()
    {
        profile_loading_text.setText("저장 중...");
        DatabaseReference profileRef = database.getReference().child("profile").child(userId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                String ageindex;
                String sexindex;
                if(userProfile.getSex() == 1)
                {
                    sexindex = "male";
                }
                else
                {
                    sexindex = "female";
                }
                ageindex = Integer.toString(userProfile.age);
                DatabaseReference indexRef = database.getReference().child("index").child(sexindex).child(ageindex).child(userProfile.location);
                indexRef.child("user").child(userId).removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        profile_loading_text.setText("저장 실패");
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        indexRef.child("number").runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                Log.d("kimurzzoo", "doTransaction");
                                if (currentData.getValue() == null){
                                    return Transaction.success(currentData);
                                }
                                Log.d("kimurzzoo", "what : " + currentData.getValue(Integer.class));
                                if(currentData.getValue(Integer.class) >= 1)
                                {
                                    Log.d("kimurzzoo", "number decreasing");
                                    currentData.setValue(currentData.getValue(Integer.class) - 1);
                                    Log.d("kimurzzoo", "after number decreasing : " + Integer.toString(currentData.getValue(Integer.class)));
                                }
                                Log.d("kimurzzoo", "after number decreasing9 : " + Integer.toString(currentData.getValue(Integer.class)));
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                if(currentData == null)
                                {
                                    return;
                                }

                                int newsex;
                                String newsexindex;
                                if(profile_malecheckbox.isChecked())
                                {
                                    newsex = 1;
                                    newsexindex = "male";
                                }
                                else
                                {
                                    newsex = 0;
                                    newsexindex = "female";
                                }
                                String newageindex = profile_age_text.getText().toString();
                                DatabaseReference newindexRef = database.getReference().child("index").child(newsexindex).child(newageindex).child(profile_locationtext.getText().toString());
                                newindexRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        newindexRef.child("number").runTransaction(new Transaction.Handler() {
                                            @NonNull
                                            @Override
                                            public Transaction.Result doTransaction(@NonNull MutableData currentData1) {
                                                if(snapshot.getValue() == null && currentData1.getValue() == null)
                                                {
                                                    //Log.d("kimurzzoo", "after number decreasing1 : " + Integer.toString(currentData.getValue(Integer.class)));
                                                    currentData1.setValue(1);
                                                    newindexRef.child("user").child(userId).setValue(userId);
                                                }
                                                else if(snapshot.getValue() != null && currentData1.getValue() == null)
                                                {
                                                    //Log.d("kimurzzoo", "after number decreasing2 : " + Integer.toString(currentData.getValue(Integer.class)));
                                                    return Transaction.success(currentData1);
                                                }
                                                else
                                                {
                                                    if(snapshot.getValue() == null && currentData1.getValue(Integer.class) > 0)
                                                    {
                                                        //Log.d("kimurzzoo", "after number decreasing3 : " + Integer.toString(currentData.getValue(Integer.class)));
                                                        currentData1.setValue(1);
                                                    }
                                                    else
                                                    {
                                                        //Log.d("kimurzzoo", "after number decreasing4 : " + Integer.toString(currentData.getValue(Integer.class)));
                                                        currentData1.setValue(currentData1.getValue(Integer.class) + 1);
                                                    }
                                                    newindexRef.child("user").child(userId).setValue(userId);
                                                }
                                                //Log.d("kimurzzoo", "after number decreasing5 : " + Integer.toString(currentData.getValue(Integer.class)));
                                                return Transaction.success(currentData1);
                                            }

                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData1) {
                                                if(snapshot.getValue() != null && currentData1.getValue() == null)
                                                {
                                                    return;
                                                }
                                                //Log.d("kimurzzoo", "after number decreasing6 : " + Integer.toString(currentData.getValue(Integer.class)));
                                                profileRef.child("age").setValue(Integer.parseInt(profile_age_text.getText().toString()));
                                                profileRef.child("job").setValue(profile_job_text.getText().toString());
                                                profileRef.child("location").setValue(profile_locationtext.getText().toString());
                                                profileRef.child("memo").setValue(profile_memo_text.getText().toString());
                                                profileRef.child("sex").setValue(newsex);
                                                //Log.d("kimurzzoo", "after number decreasing7 : " + Integer.toString(currentData.getValue(Integer.class)));
                                                profile_loading_text.setText("저장 성공");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profile_loading_text.setText("저장 실패");
            }
        });


        if(modifiedimage)
        {
            final StorageReference storageRef = storage.getReference();
            Uri file = Uri.fromFile(new File(getPath(selectedImageUri)));
            String fileStr = file.getLastPathSegment();
            String fileExtension = fileStr.substring(fileStr.lastIndexOf(".")+1,fileStr.length());
            if(!fileExtension.equals("jpg"))
            {
                toastMachine("확장자가 jpg이어야 합니다.");
                profile_loading_text.setText("프로필 사진 저장 실패");
                return;
            }
            final StorageReference profileimageRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/picture/profile."+ fileExtension);
            UploadTask uploadTask = profileimageRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    profile_loading_text.setText("프로필 사진 저장 실패");
                    toastMachine("프로필 사진이 업로드 되지 않았습니다.");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profile_loading_text.setText("프로필 사진 저장 성공!");
                }
            });
        }

        if(modifiedvideo)
        {
            final StorageReference storageRef = storage.getReference();
            Uri file = Uri.fromFile(new File(getPath(videoUri)));
            String fileStr = file.getLastPathSegment();
            String fileExtension = fileStr.substring(fileStr.lastIndexOf(".")+1,fileStr.length());
            final StorageReference profileVideoRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/video/intro."+ fileExtension);
            UploadTask uploadTask = profileVideoRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    profile_loading_text.setText("로딩 실패");
                    toastMachine("자기 소개 영상이 업로드 되지 않았습니다.");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profile_loading_text.setText("자기 소개 영상 저장 성공!");
                }
            });
        }
    }

    private void toastMachine(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if(localVideoFile != null)
        {
            Log.d("kimurzzoo","profile localVideofile destroy");
            localVideoFile.delete();
        }
        super.onDestroy();
    }

    public String getPath(Uri uri)
    {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();
        return cursor.getString(index);
    }
}