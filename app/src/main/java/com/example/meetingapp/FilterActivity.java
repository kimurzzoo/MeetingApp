package com.example.meetingapp;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FilterActivity extends Activity {
    private EditText startage;
    private EditText endage;
    private TextView location_text;
    private Button location_btn;
    private Button save_btn;
    private Button cancel_btn;
    private String[] locations = {"서울", "인천", "대전", "광주", "대구", "울산", "부산", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"};
    private AlertDialog locationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_filter);

        startage = (EditText) findViewById(R.id.filter_startage);
        endage = (EditText) findViewById(R.id.filter_endage);
        location_text = (TextView) findViewById(R.id.filter_location);
        location_btn = (Button) findViewById(R.id.filter_location_btn);
        save_btn = (Button) findViewById(R.id.filter_save_btn);
        cancel_btn = (Button) findViewById(R.id.filter_cancel_btn);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference filterRef = database.getReference().child("filter").child(mAuth.getCurrentUser().getUid());
        filterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FilterData filterData = snapshot.getValue(FilterData.class);
                if(filterData == null)
                {
                    startage.setText("0");
                    endage.setText("99");
                    location_text.setText("서울");
                }
                else
                {
                    startage.setText(Integer.toString(filterData.startage));
                    endage.setText(Integer.toString(filterData.endage));
                    location_text.setText(filterData.location);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        locationDialog = new AlertDialog.Builder(FilterActivity.this).setItems(locations, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                location_text.setText(locations[i]);
            }
        }).setTitle("지역 선택").setPositiveButton("확인", null).setNegativeButton("취소", null).create();

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationDialog.show();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startage.getText().toString().equals("") || endage.getText().toString().equals(""))
                {
                    toastMachine("원하는 나이를 입력해주세요.");
                    return;
                }

                if(Integer.parseInt(startage.getText().toString()) > Integer.parseInt(endage.getText().toString()))
                {
                    toastMachine("나이 범위가 잘 못 설정되었습니다.");
                    return;
                }

                FilterData newfilterData = new FilterData(Integer.parseInt(startage.getText().toString()), Integer.parseInt(endage.getText().toString()), location_text.getText().toString());
                filterRef.setValue(newfilterData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                });
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void toastMachine(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}