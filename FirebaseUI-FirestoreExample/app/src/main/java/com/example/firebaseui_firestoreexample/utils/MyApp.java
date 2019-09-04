package com.example.firebaseui_firestoreexample.utils;


import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.firebaseui_firestoreexample.MainActivity;
import com.example.firebaseui_firestoreexample.MyBroadcastReceiver;
import com.example.firebaseui_firestoreexample.TimeReminder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class MyApp extends Application {
    private static MyApp firstInstance;
    public static boolean updateFromServer;
    public static String titleOldVersion;
    public static long totalTime;
    public static LinkedList<String> historyTitle;
    public static HashMap<String, DocumentReference> loadToCacheMap;
    public static HashMap<String, OfflineNoteData> allNotesOfflineNoteData;
    private static boolean activityVisible;
    private static boolean activityEditNoteVisible;
    private static boolean backUpFailed;
//    makeText(c, "might not be up to date last updated:", LENGTH_SHORT).show();

    // uncaught exception handler variable
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;


    public MyApp() {
        historyTitle = new LinkedList<>();
        loadToCacheMap = new HashMap<>();
        allNotesOfflineNoteData = new HashMap<>();
        totalTime = 0;
        backUpFailed = false;
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
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

    public static Context getContext() {
//        return firstInstance;
        return firstInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        firstInstance = this;
        registerReceiver(new NetworkChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        loadRemindersAndRegisterListeners();
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

    public static boolean isBackUpFailed() {
        return backUpFailed;
    }

    public static void setBackUpFailed(boolean backUpFailed) {
        MyApp.backUpFailed = backUpFailed;
    }

    public static void loadToCache() {
        for (DocumentReference documentReference :
                loadToCacheMap.values()) {
            documentReference.get(Source.SERVER).addOnCompleteListener(task -> backUpFailed = !task.isSuccessful());
        }
    }

    private void loadRemindersAndRegisterListeners() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        long long_one_mb = 1048576L;
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
        Query collectionRemindersRef = db.collectionGroup("Reminders");
//        only the reminders from a specific user.
//        add the reminders to MyApp
        Query query = collectionRemindersRef.whereEqualTo("type", "time");
        query.get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot reminder : Objects.requireNonNull(task.getResult())) {
                TimeReminder timeReminder = reminder.toObject(TimeReminder.class);
                if (!timeReminder.getTimestamp().toDate().before(new Date()))
                    addReminderToAlarmManager(reminder);
                reminder.getReference().addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) System.err.println("Listen failed: " + e);
                    if (!timeReminder.getTimestamp().toDate().before(new Date()))
                        addReminderToAlarmManager(reminder);
                });
            }
        });

    }

    private void addReminderToAlarmManager(QueryDocumentSnapshot reminder) {
        Context c = getContext();
        TimeReminder timeReminder = reminder.toObject(TimeReminder.class);
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(c, MyBroadcastReceiver.class);
        myIntent.setAction(reminder.getId());
        myIntent.putExtra("whatsapp", false);
//                  for the noteID!

        DocumentReference d = reminder.getReference();
        CollectionReference coll = d.getParent();
        DocumentReference r = coll.getParent();
        String s = Objects.requireNonNull(r).getId();
        Toast.makeText(c,s, Toast.LENGTH_SHORT).show();
        myIntent.putExtra("documentID", Objects.requireNonNull(reminder.getReference().getParent().getParent()).getId());
//                    no need to check because it just sets the reminder again?
        // only push reminders which are in the future!
        if (alarmManager != null) {
            boolean isWorking = (PendingIntent.getBroadcast(c, 0, myIntent, PendingIntent.FLAG_NO_CREATE) != null);
            if (!isWorking) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, myIntent, 0);
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeReminder.getTimestamp().toDate().getTime(), pendingIntent);
            }
        }
    }

    // handler listener
    @SuppressWarnings("FieldCanBeLocal")
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {

                    // here I do logging of exception to a db
                    PendingIntent myActivity = PendingIntent.getActivity(getContext(),
                            192837, new Intent(getContext(), MainActivity.class),
                            PendingIntent.FLAG_ONE_SHOT);

                    Intent myIntent = new Intent(getContext(), MyBroadcastReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, myIntent, 0);

                    Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();
                    AlarmManager alarmManager;
                    alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Objects.requireNonNull(alarmManager).set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            15000, myActivity );
                    System.exit(2);

                    // re-throw critical exception further to the os (important)
                    uncaughtExceptionHandler.uncaughtException(thread, ex);
                }
            };

}