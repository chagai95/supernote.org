package com.example.firebaseui_firestoreexample.utils;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.O)
class NotificationChannels {
    private static ArrayList<NotificationChannel> channels = new ArrayList<NotificationChannel>() {{
        add(new NotificationChannel("CHANNEL_ID", "Test Channel", NotificationManager.IMPORTANCE_HIGH));
    }};


    static ArrayList<NotificationChannel> getChannels() {
        return channels;
    }
}
