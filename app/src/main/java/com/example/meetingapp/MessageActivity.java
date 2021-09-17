package com.example.meetingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.example.meetingapp.RequestFragment.getCurrentNetworkTime;

public class MessageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String uid;
    private String opUid;

    private ScrollView scrollView;
    private TextView toolbar_name;
    private ListView listView;
    private EditText message_input;
    private Button message_send_button;

    private LiveMessageAdapter liveMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        startService(new Intent(this, ForcedTerminationService.class));

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
            StrictMode.setThreadPolicy(policy);
        }

        Intent beforeFrag = getIntent();
        opUid = beforeFrag.getStringExtra("position");

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = mAuth.getCurrentUser().getUid();

        setSupportActionBar(findViewById(R.id.message_activity_toolbar));
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_24px);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        scrollView = findViewById(R.id.message_activity_scrollview);
        toolbar_name = findViewById(R.id.message_activity_name);
        listView = findViewById(R.id.message_activity_listview);
        message_input = findViewById(R.id.message_activity_edittext);
        message_send_button = findViewById(R.id.message_activity_send_button);
        liveMessageAdapter = new LiveMessageAdapter(uid, opUid);

        listView.setAdapter(liveMessageAdapter);

        DatabaseReference chatroomnameRef = database.getReference().child("chatroom").child(uid).child(opUid).child("name");
        chatroomnameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String opname = snapshot.getValue(String.class);
                toolbar_name.setText(opname);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference chatroomRef = database.getReference().child("chatroom").child(uid).child(opUid);
        DatabaseReference chatroomOpRef = database.getReference().child("chatroom").child(opUid).child(uid);
        DatabaseReference chatroomkeyRef = chatroomRef.child("roomkey");
        DatabaseReference chatroomnoreadRef = chatroomRef.child("noread");
        chatroomkeyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                 String roomkey = snapshot.getValue(String.class);
                 DatabaseReference roomRef = database.getReference().child("room").child(roomkey);

                 roomRef.addChildEventListener(new ChildEventListener() {
                     @Override
                     public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                         LiveMessageItem item = snapshot.getValue(LiveMessageItem.class);
                         liveMessageAdapter.additem(item);
                         liveMessageAdapter.notifyDataSetChanged();
                         listView.setAdapter(liveMessageAdapter);
                         setListViewHeightBasedOnChildren(listView);
                         chatroomnoreadRef.setValue(0);
                     }

                     @Override
                     public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                     }

                     @Override
                     public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

                     }

                     @Override
                     public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                     }

                     @Override
                     public void onCancelled(@NonNull @NotNull DatabaseError error) {

                     }
                 });

                message_send_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long thistime = 0;
                        try {
                            thistime = getCurrentNetworkTime();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        final long sendtime = thistime;
                        String thismessage = message_input.getText().toString();
                        LiveMessageItem senditem = new LiveMessageItem(thismessage, thistime, uid);
                        DatabaseReference sendRef = roomRef.push();
                        sendRef.setValue(senditem).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                message_input.setText("");
                                chatroomRef.child("lastDate").setValue(sendtime);
                                chatroomOpRef.child("lastDate").setValue(sendtime);
                                chatroomRef.child("lastMessage").setValue(thismessage);
                                chatroomOpRef.child("lastMessage").setValue(thismessage);
                                chatroomOpRef.child("noread").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        int noread = snapshot.getValue(Integer.class);
                                        chatroomOpRef.child("noread").setValue(noread + 1);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().detectNetwork().build();
            StrictMode.setThreadPolicy(policy);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        LiveMessageAdapter listAdapter = (LiveMessageAdapter) listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int numberOfItems = listAdapter.getCount();

        // Get total height of all items.
        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = listAdapter.getView(itemPos, null, listView);
            float px = 500 * (listView.getResources().getDisplayMetrics().density);
            item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalItemsHeight += item.getMeasuredHeight();
        }

        // Get total height of all item dividers.
        int totalDividersHeight = listView.getDividerHeight() *
                (numberOfItems - 1);
        // Get padding
        int totalPadding = listView.getPaddingTop() + listView.getPaddingBottom();

        // Set list height.
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight + totalPadding;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}

class LiveMessageItem
{
    public String message;
    public long time;
    public String uid;

    public LiveMessageItem()
    {

    }

    public LiveMessageItem(String message, long time, String uid)
    {
        this.message = message;
        this.time = time;
        this.uid = uid;
    }
}

class LiveMessageAdapter extends BaseAdapter
{
    ArrayList<LiveMessageItem> items = new ArrayList<LiveMessageItem>();
    public String uid;
    public String opUid;

    public LiveMessageAdapter()
    {

    }

    public LiveMessageAdapter(String uid, String opUid)
    {
        this.uid = uid;
        this.opUid = opUid;
    }

    public void additem(LiveMessageItem item)
    {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LiveMessageItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LiveMessageItem thisitem = items.get(i);
        if(thisitem.uid.equals(uid))
        {
            ListView_Inflater_Live_Message thisview = new ListView_Inflater_Live_Message(viewGroup.getContext());
            LiveMessageItem item = items.get(i);

            thisview.setMessage(item.message);
            try
            {
                thisview.setTime(item.time, getCurrentNetworkTime());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return thisview;
        }
        else if(thisitem.uid.equals(opUid))
        {
            ListView_Inflater_Live_Message_Op thisview = new ListView_Inflater_Live_Message_Op(viewGroup.getContext());
            LiveMessageItem item = items.get(i);

            thisview.setImage(item.uid);
            thisview.setMessage(item.message);
            try
            {
                thisview.setTime(item.time, getCurrentNetworkTime());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return thisview;
        }
        return null;
    }
}