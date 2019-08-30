package com.example.firebaseui_firestoreexample.utils;


import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.LinkedList;

public class MyApp extends Application {
    private static MyApp firstInstance;
    public static boolean updateFromServer;
    public static String titleOldVersion;
    public static long totalTime;
    public static LinkedList<String> historyTitle;
    public static LinkedList<DocumentReference> loadToCacheList;
    public static HashMap<String,OfflineNoteData> allNotesOfflineNoteData;
    private static boolean activityVisible;
    private static boolean activityEditNoteVisible;
//    makeText(c, "might not be up to date last updated:", LENGTH_SHORT).show();




    public MyApp() {
        historyTitle = new LinkedList<>();
        loadToCacheList = new LinkedList<>();
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
        super.onCreate();
        firstInstance = this;
        firstInstance.registerReceiver(new NetworkChangeReceiver(),new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }



    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityStopped() {
        activityVisible = false;
    }

    public static boolean isActivityEditNoteVisible() {
        return activityEditNoteVisible;
    }

    public static void activityEditNoteResumed() {
        activityEditNoteVisible = true;
    }

    public static void activityEditNoteStopped() {
        activityEditNoteVisible = false;
    }

    @SuppressWarnings("unused")
    public static void loadToCache(){
        for (DocumentReference documentReference:
                loadToCacheList) {
            documentReference.get();
        }
    }

}