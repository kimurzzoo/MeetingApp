package com.example.meetingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Random;

public class RequestFragment extends Fragment {
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private String uid;
    private DatabaseReference profileRef;
    private TextView request_refresh_remain;
    private Button request_refresh_button;
    private Button request_filter_button;
    private ImageView request_profile_image;
    private TextView request_profile_name;
    private TextView request_profile_location;
    private TextView request_profile_job;
    private TextView request_profile_memo;
    private Button request_press_button;
    private TextView request_remain;
    private TextView request_profile_age;
    private DatabaseReference requestProfileRef;
    private DatabaseReference userRemainRef;
    private DatabaseReference requesterRef;
    private String requesterUid;

    private String[] locations = {"서울", "인천", "대전", "광주", "대구", "울산", "부산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"};

    public static final String TIME_SERVER = "time.bora.net";

    public RequestFragment() {
    }

    public static RequestFragment newInstance(String param1, String param2) {
        RequestFragment fragment = new RequestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().startService(new Intent(getContext(), ForcedTerminationService.class));

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
            StrictMode.setThreadPolicy(policy);
        }
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        userRemainRef = database.getReference().child("userremain").child(uid);
        userRemainRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserRemain userRemain = snapshot.getValue(UserRemain.class);
                request_refresh_remain.setText(Integer.toString(userRemain.refresh));
                request_remain.setText(Integer.toString(userRemain.request));
                String mDateFormat = "yyyy-MM-dd";
                if(userRemain.lastremain != null)
                {
                    Date lastRemaindate = new Date(Long.parseLong(userRemain.lastremain));
                    String lastRemaindatestring = new SimpleDateFormat(mDateFormat).format(lastRemaindate);
                    Date today = null;
                    Long longtime = 0l;
                    try {
                        longtime  = getCurrentNetworkTime();
                        today = new Date(longtime);
                    }
                    catch (IOException e)
                    {

                    }
                    String todaystring = new SimpleDateFormat(mDateFormat).format(today);
                    if(lastRemaindatestring.compareTo(todaystring) < 0)
                    {
                        userRemainRef.child("lastremain").setValue(longtime.toString());
                        userRemainRef.child("refresh").setValue(20);
                        userRemainRef.child("request").setValue(3);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        requesterRef = database.getReference().child("requester").child(uid);
        requesterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requesterUid = snapshot.getValue(String.class);
                if(requesterUid != null)
                {
                    requestProfileRef = database.getReference().child("profile").child(requesterUid);
                    requestProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            request_profile_name.setText(userProfile.getName());
                            request_profile_age.setText(Integer.toString(userProfile.getAge()));
                            request_profile_location.setText(userProfile.location);
                            request_profile_job.setText(userProfile.getJob());
                            request_profile_memo.setText(userProfile.getMemo());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    StorageReference storageRef = storage.getReference();
                    StorageReference imageRef = storageRef.child(requesterUid + "/picture/profile.jpg");

                    /*try
                    {
                        if(localImageFile != null)
                        {
                            localImageFile.delete();
                        }

                        localImageFile = File.createTempFile("user_image", ".jpg", getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES));

                        imageRef.getFile(localImageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                request_profile_image.setImageURI(Uri.fromFile(localImageFile));
                            }
                        });
                    }
                    catch(IOException e)
                    {
                    }*/

                    Glide.with(getContext()).load(imageRef).into(request_profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        request_refresh_remain = (TextView) view.findViewById(R.id.request_refresh_remain);
        request_refresh_button = (Button) view.findViewById(R.id.request_refresh_button);
        request_filter_button = (Button) view.findViewById(R.id.request_filter_button);
        request_profile_image = (ImageView) view.findViewById(R.id.request_profile_image);
        request_profile_name = (TextView) view.findViewById(R.id.request_profile_name);
        request_profile_location = (TextView) view.findViewById(R.id.request_profile_location);
        request_profile_job = (TextView) view.findViewById(R.id.request_profile_job);
        request_profile_memo = (TextView) view.findViewById(R.id.request_profile_memo);
        request_press_button = (Button) view.findViewById(R.id.request_press_button);
        request_remain = (TextView) view.findViewById(R.id.request_remain);
        request_profile_age = (TextView) view.findViewById(R.id.request_profile_age);
        request_filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), FilterActivity.class);
                startActivity(intent);
            }
        });

        request_refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRemainRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserRemain userRemain = snapshot.getValue(UserRemain.class);
                        if(userRemain.refresh > 0)
                        {
                            userRemainRef.child("refresh").setValue(userRemain.refresh - 1);
                            Long lastremaindate = 0l;
                            try {
                                lastremaindate = getCurrentNetworkTime();
                            }
                            catch (IOException e)
                            {

                            }
                            userRemainRef.child("lastremain").setValue(Long.toString(lastremaindate));
                            updateRequester();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        request_press_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRemainRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserRemain userRemain = snapshot.getValue(UserRemain.class);
                        if(userRemain.request > 0)
                        {
                            userRemainRef.child("request").setValue(userRemain.request - 1);
                            Long lastremaindate = 0l;
                            try {
                                lastremaindate = getCurrentNetworkTime();
                            }
                            catch (IOException e)
                            {

                            }
                            userRemainRef.child("lastremain").setValue(Long.toString(lastremaindate));
                            DatabaseReference requestedRef = database.getReference().child("requested").child(requesterUid).child(uid);
                            requestedRef.setValue(uid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        return view;
    }

    public static long getCurrentNetworkTime() throws IOException {
        NTPUDPClient lNTPUDPClient = new NTPUDPClient();
        lNTPUDPClient.setDefaultTimeout(3000);
        lNTPUDPClient.open();
        InetAddress lInetAddress = InetAddress.getByName(TIME_SERVER);
        TimeInfo lTimeInfo = lNTPUDPClient.getTime(lInetAddress);
        lNTPUDPClient.close();
        return lTimeInfo.getReturnTime();
    }

    @Override
    public void onDestroy() {
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().detectNetwork().build();
            StrictMode.setThreadPolicy(policy);
        }
        super.onDestroy();
    }

    public void updateRequester()
    {
        Log.d("kimurzzoo", "updateRequester start");
        profileRef = database.getReference().child("profile").child(uid);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                int userSex = userProfile.sex;
                Log.d("kimurzzoo", "updateRequester get sex");
                DatabaseReference filterRef = database.getReference().child("filter").child(uid);
                filterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FilterData filterData = snapshot.getValue(FilterData.class);
                        Random random = new Random();
                        DatabaseReference filteredRequesterRef;
                        int requesterage = 0;
                        String requesterlocation = null;
                        String newUserSex;
                        if(filterData == null)
                        {
                            Log.d("kimurzzoo", "updateRequester filter null");
                            requesterage = random.nextInt(98) + 1;
                            requesterlocation = locations[random.nextInt(locations.length)];
                        }
                        else
                        {
                            Log.d("kimurzzoo", "updateRequester filter not null");
                            requesterage = random.nextInt(filterData.endage - filterData.startage) + filterData.startage;
                            requesterlocation = filterData.location;
                        }
                        if(userSex == 1)
                        {
                            newUserSex = "male";
                        }
                        else
                        {
                            newUserSex = "female";
                        }
                        Log.d("kimurzzoo", "updateRequester " + newUserSex + " " + Integer.toString(requesterage) + requesterlocation);
                        filteredRequesterRef = database.getReference().child("index").child(newUserSex).child(Integer.toString(requesterage)).child(requesterlocation);
                        filteredRequesterRef.child("number").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Integer filteredRequestersnumber = snapshot.getValue(Integer.class);
                                if(filteredRequestersnumber == null)
                                {
                                    Log.d("kimurzzoo", "updateRequester number null");
                                    updateRequester();
                                    return;
                                }
                                else
                                {
                                    Log.d("kimurzzoo", "updateRequester number not null");
                                    int filterednumber = random.nextInt(filteredRequestersnumber.intValue());
                                    filteredRequesterRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int i = 0;
                                            for(DataSnapshot postSnapshot : snapshot.getChildren())
                                            {
                                                if(i >= filterednumber)
                                                {
                                                    String finalmap = postSnapshot.getValue(String.class);
                                                    Log.d("kimurzzoo", "filtered map : " + finalmap);
                                                    requesterRef.setValue(finalmap);
                                                    break;
                                                }
                                                i++;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}