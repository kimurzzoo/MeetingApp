package com.example.meetingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IntroductionActivity extends AppCompatActivity {
    private VideoView videoThumbnail;
    private TextView loadingtext;
    private TextView codetext;
    private Button videomakingbutton;
    private Button gallerybutton;
    private Button startbutton;
    private FirebaseAuth mAuth;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private Activity activity;
    private FirebaseStorage storage;
    private Uri videoUri = null;
    private UploadTask uploadTask;
    private File videoFile = null;

    public IntroductionActivity()
    {

    }
    public IntroductionActivity(Activity context) {
        this.activity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        startService(new Intent(this, ForcedTerminationService.class));

        if(ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1001);
        }

        videomakingbutton = (Button) findViewById(R.id.videomakingbutton_intro);
        videoThumbnail = (VideoView) findViewById(R.id.videothumbnail);
        loadingtext = (TextView) findViewById(R.id.loadingtextintro);
        codetext = (TextView) findViewById(R.id.code_intro);
        gallerybutton = (Button) findViewById(R.id.gallery_button_intro);
        startbutton = (Button) findViewById(R.id.startbutton_intro);

        videoThumbnail.setMediaController(new MediaController(this));

        mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference profileRef = database.getReference().child("profile").child(userId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                codetext.setText(userProfile.authnumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        videomakingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });

        gallerybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, 200);
            }
        });

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoUri == null)
                {
                    toastMachine("선택된 영상이 없습니다.");
                    return;
                }
                loadingtext.setText("로딩중...");
                storage = FirebaseStorage.getInstance();
                final StorageReference storageRef = storage.getReference();
                Uri file = Uri.fromFile(new File(getPath(videoUri)));
                String fileStr = file.getLastPathSegment();
                String fileExtension = fileStr.substring(fileStr.lastIndexOf(".")+1,fileStr.length());
                final StorageReference profileRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/video/intro."+ fileExtension);
                uploadTask = profileRef.putFile(file);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        loadingtext.setText("로딩 실패");
                        toastMachine("자기 소개 영상이 업로드 되지 않았습니다.");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        String userId = mAuth.getCurrentUser().getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference profileRef = database.getReference().child("profile").child(userId);

                        profileRef.child("introductiontutorial").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                loadingtext.setText("로딩완료!");
                                Intent intent = new Intent(IntroductionActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults)
    {
        if(requestCode == 1001 && grandResults.length == REQUIRED_PERMISSIONS.length)
        {
            boolean check_result = true;

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (!check_result ) {
                toastMachine("권한 허용이 안 되어 어플리케이션을 종료합니다.");
                moveTaskToBack(true);
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
            }
            // Continue only if the File was successfully created
            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(this,
                        "com.example.meetingapp.fileprovider",
                        videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                galleryAddVid();
                startActivityForResult(takeVideoIntent, 300);
            }
        }

    }

    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = new File("storage/emulated/0/DCIM/Camera");
        if(!storageDir.exists()){
            Log.d("kimurzzoo", "create inner mkdir start");
            storageDir.mkdirs();
            Log.d("kimurzzoo", "create inner mkdir end");
        }
        File image = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentVideoPath = image.getAbsolutePath();
        return image;
    }

    String currentVideoPath;

    private void galleryAddVid() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentVideoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            videoUri = intent.getData();
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
                videoThumbnail.setVideoURI(videoUri);
            }
        }
        else if (requestCode == 300 && resultCode == RESULT_OK) {
            galleryAddVid();
        }
    }

    private void toastMachine(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        if(videoFile != null)
        {
            videoFile.delete();
        }
        super.onDestroy();
    }
}