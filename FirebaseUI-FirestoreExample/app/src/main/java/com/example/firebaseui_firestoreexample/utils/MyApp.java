package com.example.firebaseui_firestoreexample.utils;


import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.util.HashMap;
import java.util.LinkedList;

public class MyApp extends Application {
    private static MyApp firstInstance;
    public static boolean updateFromServer;
    public static String titleOldVersion;
    public static long totalTime;
    public static LinkedList<String> historyTitle;
    public static HashMap<String,OfflineNoteData> allNotesOfflineNoteData;
//    makeText(c, "might not be up to date last updated:", LENGTH_SHORT).show();




    public MyApp() {
        historyTitle = new LinkedList<>();
        allNotesOfflineNoteData = new HashMap<>();
        totalTime = 0;
    }

    public static synchronized MyApp getFirstInstance() {
        if (firstInstance == null) {
            firstInstance = new MyApp();
            synchronized (MyApp.class) {
                if (firstInstance == null)
                    firstInstance = new MyApp();
            }
        }
        return firstInstance;
    }

    @SuppressWarnings("unused")
    public static Context getContext(){
        return firstInstance;
        // or return firstInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        firstInstance = this;
        super.onCreate();
        firstInstance.registerReceiver(new NetworkChangeReceiver(),new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}