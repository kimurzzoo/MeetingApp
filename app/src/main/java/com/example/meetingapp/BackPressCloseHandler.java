package com.example.meetingapp;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            toast.cancel();
            activity.moveTaskToBack(true);
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                activity.finishAndRemoveTask();
            } else {
                activity.finish();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public void showGuide() {
        toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
