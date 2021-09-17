package com.example.meetingapp;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class FCM extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        Log.d("kimurzzoo", "Refreshed token: " + token);

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
}
