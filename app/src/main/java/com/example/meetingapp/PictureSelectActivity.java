package com.example.meetingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureSelectActivity extends AppCompatActivity {

    private Button selectPictureFromGallery;
    private Button nextButton;
    private Button capturebtn;
    private ImageView selectedPicture;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
    private FirebaseStorage storage;
    private Uri selectedImageUri = null;
    private UploadTask uploadTask;
    private Activity activity;
    private TextView loadingtext;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private File photoFile = null;


    public PictureSelectActivity(Activity context) {
        this.activity = context;
    }

    public PictureSelectActivity()
    {

    }

    //private Uri profilepictureUri;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_select);

        startService(new Intent(this, ForcedTerminationService.class));

        loadingtext = (TextView) findViewById(R.id.loadingtextpicture);

        if(ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1001);
        }

        capturebtn = (Button) findViewById(R.id.capture);
        capturebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        selectedPicture = (ImageView) findViewById(R.id.profile_image);
        selectPictureFromGallery = (Button) findViewById(R.id.pictureselect);
        selectPictureFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/");
                startActivityForResult(intent, 200);
            }
        });

        nextButton = (Button) findViewById(R.id.nextbutton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedImageUri != null)
                {
                    loadingtext.setText("로딩중...");
                    storage = FirebaseStorage.getInstance();
                    final StorageReference storageRef = storage.getReference();
                    Uri file = Uri.fromFile(new File(getPath(selectedImageUri)));
                    String fileStr = file.getLastPathSegment();
                    String fileExtension = fileStr.substring(fileStr.lastIndexOf(".")+1,fileStr.length());
                    if(!fileExtension.equals("jpg"))
                    {
                        toastMachine("확장자가 jpg이어야 합니다.");
                        loadingtext.setText("로딩 실패");
                        return;
                    }
                    final StorageReference profileRef = storageRef.child(FirebaseAuth.getInstance().getUid()+"/picture/profile."+ fileExtension);
                    uploadTask = profileRef.putFile(file);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            loadingtext.setText("로딩 실패");
                            toastMachine("프로필 사진이 업로드 되지 않았습니다.");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            String userId = mAuth.getCurrentUser().getUid();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference profileRef = database.getReference().child("profile").child(userId);

                            profileRef.child("pictureselecttutorial").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadingtext.setText("로딩완료!");
                                    Intent intent = new Intent(PictureSelectActivity.this, IntroductionActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    });
                }
                else
                {
                    toastMachine("프로필 사진이 선택되지 않았습니다.");
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            selectedPicture.setImageURI(selectedImageUri);
        }
        else if(requestCode == 300 && resultCode == RESULT_OK)
        {
            Log.d("kimurzzoo", "capture act result!");
            galleryAddPic();
        }
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

    private void toastMachine(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void dispatchTakePictureIntent() {
        Log.d("kimurzzoo", "dispatch start");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                Log.d("kimurzzoo", "create start");
                photoFile = createImageFile();
                Log.d("kimurzzoo", "create end");
            } catch (IOException ex) {
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d("kimurzzoo", "uri start");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.meetingapp.fileprovider",
                        photoFile);
                Log.d("kimurzzoo", "putextra start");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d("kimurzzoo", "take act start");
                startActivityForResult(takePictureIntent, 300);
            }
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        Log.d("kimurzzoo", "create inner start");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        Log.d("kimurzzoo", "create inner dir start");
        File storageDir = new File("storage/emulated/0/DCIM/Camera");
        Log.d("kimurzzoo", "create inner dir end");
        if(!storageDir.exists()){
            Log.d("kimurzzoo", "create inner mkdir start");
            storageDir.mkdirs();
            Log.d("kimurzzoo", "create inner mkdir end");
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.d("kimurzzoo", "create inner temp img end");

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d("kimurzzoo", "currentPhotoPath : " + currentPhotoPath);
        return image;
    }

    private void galleryAddPic() {
        Log.d("kimurzzoo", "media scanning...");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onDestroy() {
        if(photoFile != null)
        {
            photoFile.delete();
        }
        super.onDestroy();
    }
}
