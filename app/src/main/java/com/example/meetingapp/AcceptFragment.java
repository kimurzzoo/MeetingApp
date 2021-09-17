package com.example.meetingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AcceptFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AcceptFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String uid;

    private ListView acceptListView;
    private AcceptAdapter acceptAdapter;

    public AcceptFragment() {
    }

    public static AcceptFragment newInstance(String param1, String param2) {
        AcceptFragment fragment = new AcceptFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().startService(new Intent(getContext(), ForcedTerminationService.class));

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accept, container, false);
        acceptListView = (ListView) view.findViewById(R.id.accept_listview);
        acceptAdapter = new AcceptAdapter();
        acceptListView.setAdapter(acceptAdapter);

        DatabaseReference requestedRef = database.getReference().child("requested").child(uid);
        requestedRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                String requestedUid = snapshot.getValue(String.class);

                DatabaseReference requestedProfileRef = database.getReference().child("profile").child(requestedUid);
                requestedProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        AcceptItem item = new AcceptItem(requestedUid, userProfile.name, Integer.toString(userProfile.age), userProfile.location, userProfile.job);
                        acceptAdapter.additem(item);
                        acceptAdapter.notifyDataSetChanged();
                        acceptListView.setAdapter(acceptAdapter);
                        setListViewHeightBasedOnChildren(acceptListView);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                String requestedUid = snapshot.getValue(String.class);
                AcceptItem item = acceptAdapter.getItemByUid(requestedUid);
                Log.d("kimurzzoo", "item uid : " + item.requestedUid);
                int index = acceptAdapter.getIdByItem(item);
                Log.d("kimurzzoo", "item index : " + index);
                acceptAdapter.removeitem(index);
                acceptAdapter.notifyDataSetChanged();
                acceptListView.setAdapter(acceptAdapter);
                setListViewHeightBasedOnChildren(acceptListView);
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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        AcceptAdapter listAdapter = (AcceptAdapter) listView.getAdapter();
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

class AcceptItem
{
    public String requestedUid;
    public String name;
    public String age;
    public String location;
    public String job;

    public AcceptItem(String requestedUid, String name, String age, String location, String job)
    {
        this.requestedUid = requestedUid;
        this.name = name;
        this.age = age;
        this.location = location;
        this.job = job;
    }
}

class AcceptAdapter extends BaseAdapter
{
    ArrayList<AcceptItem> items = new ArrayList<AcceptItem>();

    public void additem(AcceptItem item)
    {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public AcceptItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public AcceptItem getItemByUid(String uid)
    {
        for(int i = 0; i < getCount(); i++)
        {
            if(items.get(i).requestedUid.equals(uid))
            {
                return getItem(i);
            }
        }
        return null;
    }

    public void removeitem(int itemId)
    {
        items.remove(itemId);
    }

    public int getIdByItem(AcceptItem item)
    {
        for(int i = 0; i < getCount(); i++)
        {
            if(item.requestedUid.equals(items.get(i).requestedUid))
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ListView_Inflater thisview = new ListView_Inflater(viewGroup.getContext());

        AcceptItem item = items.get(i);
        thisview.setImage(item.requestedUid);
        thisview.setName(item.name);
        thisview.setAge(item.age);
        thisview.setLocation(item.location);
        thisview.setJob(item.job);
        thisview.setButton(items, i, item.requestedUid, this);

        return thisview;
    }
}