package com.example.meetingapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ListView_Inflater_Live_Message_Op extends LinearLayout {

    private ImageView image;
    private TextView message;
    private TextView time;

    public ListView_Inflater_Live_Message_Op(Context context)
    {
        super(context);
        init(context);
    }

    public ListView_Inflater_Live_Message_Op(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.live_message_item_op, this, true);

        image = findViewById(R.id.live_message_item_op_image);
        message = findViewById(R.id.live_message_item_op_message);
        time = findViewById(R.id.live_message_item_op_time);
    }

    public void setImage(String opUid)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child(opUid).child("picture/profile.jpg");

        Glide.with(getContext()).load(imageRef).into(image);
    }

    public void setMessage(String messagetext)
    {
        message.setText(messagetext);
    }

    public void setTime(long timevalue, long todayDatelong)
    {
        String yMdFormat = "yyyy-MM-dd";
        String MdFormat = "MM-dd";
        String HmFormat = "a HH:mm";
        String yFormat = "yyyy";

        Date Messagedate = new Date(timevalue);
        Date todayDate = new Date(todayDatelong);

        String MessageTime = new SimpleDateFormat(yMdFormat).format(Messagedate);
        String todayMessageTime = new SimpleDateFormat(yMdFormat).format(todayDate);
        String newMessageTime = null;

        if(MessageTime.compareTo(todayMessageTime) < 0)
        {
            String Messageyear = new SimpleDateFormat(yFormat).format(Messagedate);
            String todayMessageyear = new SimpleDateFormat(yFormat).format(todayDate);

            if(Messageyear.compareTo(todayMessageyear) < 0)
            {
                newMessageTime = MessageTime;
            }
            else
            {
                newMessageTime = new SimpleDateFormat(MdFormat).format(Messagedate);
            }
        }
        else
        {
            newMessageTime = new SimpleDateFormat(HmFormat).format(Messagedate);
        }

        time.setText(newMessageTime);
    }
}
