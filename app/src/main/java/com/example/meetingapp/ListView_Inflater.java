package com.example.meetingapp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.example.meetingapp.RequestFragment.getCurrentNetworkTime;

public class ListView_Inflater extends LinearLayout {
    private ImageView imageView;
    private TextView name;
    private TextView age;
    private TextView location;
    private TextView job;
    private Button acceptButton;
    private Button refuseButton;

    public ListView_Inflater(Context context)
    {
        super(context);
        init(context);
    }

    public ListView_Inflater(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_accept_item, this, true);

        imageView = (ImageView) findViewById(R.id.accept_item_image);
        name = (TextView) findViewById(R.id.accept_item_name);
        age = (TextView) findViewById(R.id.accept_item_age);
        location = (TextView) findViewById(R.id.accept_item_location);
        job = (TextView) findViewById(R.id.accept_item_job);
        acceptButton = (Button) findViewById(R.id.accept_item_acceptButton);
        refuseButton = (Button) findViewById(R.id.accept_item_refuseButton);
    }

    public void setImage(String uid)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child(uid).child("picture/profile.jpg");

        Glide.with(getContext()).load(imageRef).into(imageView);
    }

    public void setName(String realname)
    {
        name.setText(realname);
    }

    public void setJob(String realjob)
    {
        job.setText(realjob);
    }

    public void setAge(String realage) {age.setText(realage);}

    public void setLocation(String reallocation) {location.setText(reallocation);}

    public void setButton(ArrayList<AcceptItem> items, int position, String uid, AcceptAdapter acceptAdapter)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference matchingRef1 = database.getReference().child("chatroom").child(userId).child(uid);
        DatabaseReference matchingRef2 = database.getReference().child("chatroom").child(uid).child(userId);

        DatabaseReference requestedRef = database.getReference().child("requested").child(userId).child(uid);

        acceptButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference roomkeyRef = database.getReference().child("room").push();
                String roomkey = roomkeyRef.getKey();
                Log.d("kimurzzoo", "roomkey : " + roomkey);
                matchingRef1.child("roomkey").setValue("asdf");
                matchingRef2.child("roomkey").setValue("asdf");
                matchingRef1.child("noread").setValue(1);
                matchingRef2.child("noread").setValue(1);

                DatabaseReference profileRef1 = database.getReference().child("profile").child(userId);
                DatabaseReference profileRef2 = database.getReference().child("profile").child(uid);

                long nowtime = 0;
                try {
                    nowtime = getCurrentNetworkTime();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                final long nowtimefinal = nowtime;

                profileRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        LiveMessageItem liveMessageItem = new LiveMessageItem(userProfile.authnumber, nowtimefinal, userId);
                        roomkeyRef.push().setValue(liveMessageItem);

                        profileRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot1) {
                                UserProfile opProfile = snapshot1.getValue(UserProfile.class);
                                LiveMessageItem liveMessageItem1 = new LiveMessageItem(opProfile.authnumber, nowtimefinal, uid);
                                roomkeyRef.push().setValue(liveMessageItem1);

                                MessageItem messageItem1 = new MessageItem(uid, opProfile.name, opProfile.authnumber, nowtimefinal, 1);
                                MessageItem messageItem2 = new MessageItem(userId, userProfile.name, userProfile.authnumber, nowtimefinal, 1);

                                matchingRef1.setValue(messageItem1);
                                matchingRef2.setValue(messageItem2);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

                requestedRef.removeValue();
            }
        });

        refuseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestedRef.removeValue();
            }
        });
    }
}
