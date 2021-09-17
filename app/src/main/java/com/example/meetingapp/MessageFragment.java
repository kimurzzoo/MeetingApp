package com.example.meetingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

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
import java.util.HashMap;

import static com.example.meetingapp.RequestFragment.getCurrentNetworkTime;

public class MessageFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String uid;

    private ListView message_listview;
    private MessageAdapter messageAdapter;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
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
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        uid = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("kimurzzoo", "oncreateview");
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        message_listview = (ListView) view.findViewById(R.id.message_listview);
        messageAdapter = new MessageAdapter();
        message_listview.setAdapter(messageAdapter);
        DatabaseReference chatRoomReference = database.getReference().child("chatroom").child(uid);

        chatRoomReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                MessageItem item = snapshot.getValue(MessageItem.class);
                messageAdapter.additem(item);
                messageAdapter.notifyDataSetChanged();
                message_listview.setAdapter(messageAdapter);
                setListViewHeightBasedOnChildren(message_listview);
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                MessageItem item = snapshot.getValue(MessageItem.class);
                MessageItem thisitem = messageAdapter.getItemByUid(item.uid);
                thisitem.lastMessage = item.lastMessage;
                thisitem.lastDate = item.lastDate;
                messageAdapter.notifyDataSetChanged();
                message_listview.setAdapter(messageAdapter);
                setListViewHeightBasedOnChildren(message_listview);
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                MessageItem item = snapshot.getValue(MessageItem.class);
                int index = messageAdapter.getIdByItem(item);
                messageAdapter.removeitem(index);
                messageAdapter.notifyDataSetChanged();
                message_listview.setAdapter(messageAdapter);
                setListViewHeightBasedOnChildren(message_listview);
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        return view;
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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        MessageAdapter listAdapter = (MessageAdapter) listView.getAdapter();
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

class MessageItem{
    public String uid;
    public String name;
    public String lastMessage;
    public long lastDate;
    public int noread;

    public MessageItem()
    {

    }

    public MessageItem(String opUid, String name, String lastMessage, long lastDate, int noread)
    {
        this.uid = opUid;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastDate = lastDate;
        this.noread = noread;
    }
}

class MessageAdapter extends BaseAdapter {
    ArrayList<MessageItem> items = new ArrayList<MessageItem>();

    public void additem(MessageItem item)
    {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public MessageItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void removeitem(int itemId)
    {
        items.remove(itemId);
    }

    public MessageItem getItemByUid(String uid)
    {
        for(int i = 0; i < getCount(); i++)
        {
            if(items.get(i).uid.equals(uid))
            {
                return getItem(i);
            }
        }
        return null;
    }

    public int getIdByItem(MessageItem item)
    {
        for(int i = 0; i < getCount(); i++)
        {
            if(item.uid.equals(items.get(i).uid))
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        ListView_Inflater_Message thisview = new ListView_Inflater_Message(viewGroup.getContext());
        MessageItem item = items.get(i);
        thisview.setImage(item.uid);
        thisview.setName(item.name);
        thisview.setLastMessage(item.lastMessage);
        try {
            thisview.setLastDate(item.lastDate, getCurrentNetworkTime());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        thisview.setClicked(i, items);
        thisview.setNoread(item.noread);

        return thisview;
    }
}