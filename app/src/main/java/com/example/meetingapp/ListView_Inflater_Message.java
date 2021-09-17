package com.example.meetingapp;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListView_Inflater_Message extends LinearLayout {
    private LinearLayout clickedOp;
    private ImageView imageView;
    private TextView name;
    private TextView lastMessage;
    private TextView lastDate;
    private TextView noread;

    public ListView_Inflater_Message(Context context)
    {
        super(context);
        init(context);
    }

    public ListView_Inflater_Message(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.message_item, this, true);

        clickedOp = findViewById(R.id.message_item_layout);
        imageView = findViewById(R.id.message_item_image);
        name = findViewById(R.id.message_item_name);
        lastMessage = findViewById(R.id.message_item_lastMessage);
        lastDate = findViewById(R.id.message_item_lastDate);
        noread = findViewById(R.id.message_item_noread);
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

    public void setLastMessage (String realLastMessage) { lastMessage.setText(realLastMessage);}

    public void setLastDate (long realLastMessage, long todayDatelong){
        String yMdFormat = "yyyy-MM-dd";
        String MdFormat = "MM-dd";
        String HmFormat = "a HH:mm";
        String yFormat = "yyyy";

        Date lastMessagedate = new Date(realLastMessage);
        Date todayDate = new Date(todayDatelong);

        String lastMessageTime = new SimpleDateFormat(yMdFormat).format(lastMessagedate);
        String todayMessageTime = new SimpleDateFormat(yMdFormat).format(todayDate);
        String newlastMessageTime = null;

        if(lastMessageTime.compareTo(todayMessageTime) < 0)
        {
            String lastMessageyear = new SimpleDateFormat(yFormat).format(lastMessagedate);
            String todayMessageyear = new SimpleDateFormat(yFormat).format(todayDate);

            if(lastMessageyear.compareTo(todayMessageyear) < 0)
            {
                newlastMessageTime = lastMessageTime;
            }
            else
            {
                newlastMessageTime = new SimpleDateFormat(MdFormat).format(lastMessagedate);
            }
        }
        else
        {
            newlastMessageTime = new SimpleDateFormat(HmFormat).format(lastMessagedate);
        }
        lastDate.setText(newlastMessageTime);
    }

    public void setNoread(int noreadint)
    {
        if(noreadint == 0)
        {
            noread.setText("");
        }
        else
        {
            noread.setText(Integer.toString(noreadint));
        }
    }

    public void setClicked(int i, ArrayList<MessageItem> items)
    {
        clickedOp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MessageActivity.class);
                intent.putExtra("position", items.get(i).uid);
                view.getContext().startActivity(intent);
            }
        });
    }
}
