package com.example.firebaseui_firestoreexample.utils;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.TaskStackBuilder;

import com.example.firebaseui_firestoreexample.EditNoteActivity;
import com.example.firebaseui_firestoreexample.LocationReminder;
import com.example.firebaseui_firestoreexample.MainActivity;
import com.example.firebaseui_firestoreexample.MyBroadcastReceiver;
import com.example.firebaseui_firestoreexample.NotificationHelper;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.TimeReminder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MyApp extends Application {
    private static MyApp firstInstance;
    public static boolean updateFromServer;
    public static String titleOldVersion;
    public static long totalTime;
    public static HashMap<String, OfflineNoteData> allNotesOfflineNoteData;
    public static HashMap<String, DocumentReference> timeReminders;
    public static HashMap<String, DocumentReference> locationReminders;
    private static boolean activityVisible;
    private static boolean activityEditNoteVisible;
    private static boolean backUpFailed;
    public static boolean appStarted;
    //    makeText(c, "might not be up to date last updated:", LENGTH_SHORT).show();
    FirebaseFirestore db;
    public static DocumentReference forceStop;
    public static boolean appInternInternetOffToggle;
    public static boolean autoInternInternetOffWhenE;
    public static TrafficLight lastTrafficLightState;


    // uncaught exception handler variable
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;


    public MyApp() {
        allNotesOfflineNoteData = new HashMap<>();
        totalTime = 0;
        backUpFailed = false;
        autoInternInternetOffWhenE = false;
        appInternInternetOffToggle = false;
        appStarted = false;
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        timeReminders = new HashMap<>();
        locationReminders = new HashMap<>();
//        startAppOffline();

        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    /*private void startAppOffline() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("utils").document("startAppOffline")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getData()).get("startAppOffline")) {
                    db.disableNetwork();
                    MyApp.appInternInternetOffToggle = true;
                    new Thread(() -> {
                        try {
                            TimeUnit.SECONDS.sleep(4);
                            startActivity(new Intent(getContext(), MainActivity.class));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });
    }*/

    //    tried in on create
    @SuppressWarnings("unused")
    private void setTelephonyListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager == null)
            Log.d("TM", "TM is null");
        else
            telephonyManager.listen(new PhoneStateListener() {

                @Override
                public void onCellInfoChanged(List<CellInfo> cellInfo) {
                    super.onCellInfoChanged(cellInfo);
                    for (CellInfo ci : cellInfo) {
                        if (ci instanceof CellInfoGsm) {
                            Log.d("TAG", "This has 2G");
                        } else if (ci instanceof CellInfoLte) {
                            Log.d("TAG", "This has 4G");
                        } else {
                            Log.d("TAG", "This has 3G");
                        }
                    }
                }


            }, PhoneStateListener.LISTEN_CELL_INFO);
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
        db = FirebaseFirestore.getInstance();
//        long long_one_mb = 1048576L;
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
        //startAppOffline();
        forceStop = db.collection("utils").document("forceStop");
//        setTelephonyListener();
//      trying to add a listener in order to ensure all reminders get added instantly on all devices -
//      stuck because you can not get a specific reminder using it's id from a sub collection(Query)
//      possible solution - add the time(value) and the noteID(key) perhaps as a map. - several maps for different reminder types.
        /*db.collection("utils").document("device_4")
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) System.err.println("Listen failed: " + e);
                    //noinspection ConstantConditions
                    if ((boolean)documentSnapshot.getData().get("addedReminder")){
                        documentSnapshot.getReference().update("addedReminder",false);
                        db.collection("utils").document("device_5")
                                .get().addOnCompleteListener(task -> {
                                   if(task.isSuccessful())
                                       for (String reminderID:
                                               (ArrayList<String>) Objects.requireNonNull(Objects.requireNonNull(task.getResult().getData()).get("new_reminders"))) {
                                           db.collectionGroup("Reminders").
                                       }
                                });

                    }
                });*/
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
        FirebaseFirestore.getInstance().collection("Notebook").whereEqualTo("loadToCache", true)
                .get(Source.SERVER).addOnCompleteListener(task -> backUpFailed = !task.isSuccessful());
        FirebaseFirestore.getInstance().collectionGroup("Reminders").whereGreaterThanOrEqualTo("timestamp", new Timestamp(new Date()))
                .whereLessThanOrEqualTo("timestamp", new Timestamp(getUntilWhen()))
                .whereEqualTo("type", "time")
                .get(Source.SERVER).addOnCompleteListener(task -> backUpFailed = !task.isSuccessful());
    }

    private static Date getUntilWhen() {
        Date currentDate = new Date();

        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);


        c.add(Calendar.HOUR, 24); // or 24*7...

        // convert calendar to date
        return c.getTime();
    }

    private void loadRemindersAndRegisterListeners() {
        db.collectionGroup("Reminders").whereGreaterThanOrEqualTo("timestamp", new Timestamp(new Date()))
                .whereEqualTo("type", "time")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                for (QueryDocumentSnapshot reminder : Objects.requireNonNull(task.getResult())) {
                 /*   TimeReminder timeReminder = reminder.toObject(TimeReminder.class);
                if (!timeReminder.getTimestamp().toDate().before(new Date()))
                    addReminderToAlarmManager(reminder);*/
                    DocumentReference reminderDocRef = reminder.getReference();
                    timeReminders.put(reminderDocRef.getId(), reminderDocRef);
                    reminderDocRef.addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null)
                            System.err.println("Listen failed: " + e);
                        addReminderToAlarmManager(Objects.requireNonNull(documentSnapshot));
                        Log.i("ReminderID", documentSnapshot.getId());
                    });
                }
        });
        db.collectionGroup("Reminders")
                .whereEqualTo("type", "location")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                for (QueryDocumentSnapshot reminder : Objects.requireNonNull(task.getResult())) {
                    DocumentReference reminderDocRef = reminder.getReference();
                    locationReminders.put(reminderDocRef.getId(), reminderDocRef);
                    reminderDocRef.addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null)
                            System.err.println("Listen failed: " + e);
                        addReminderToLocationManager(Objects.requireNonNull(documentSnapshot),getContext());
                        Log.i("ReminderID", documentSnapshot.getId());
                    });
                }
        });

    }

    @SuppressLint("MissingPermission")
    public static void addReminderToLocationManager(DocumentSnapshot reminder,Context c) {
        LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                LocationReminder locationReminder = Objects.requireNonNull(reminder.toObject(LocationReminder.class));
                float distanceInMeters = locationReminder.getLocation().distanceTo(location);
                if (locationReminder.getRadius() > (double) distanceInMeters) {
                    Log.i("location", location.getLongitude() + " " + location.getLatitude());
                    Toast.makeText(getContext(), location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                    createNotificationForLocationReminder(reminder.getReference(),getContext());
                }

            }


            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                c.startActivity(i);
            }
        };


        Objects.requireNonNull(locationManager).requestLocationUpdates("gps", 500, 1, listener);
    }

    public static void createNotificationForLocationReminder(DocumentReference reminderDocRef,Context c) {
        CollectionReference coll = reminderDocRef.getParent();
        DocumentReference r = coll.getParent();
        String s = Objects.requireNonNull(r).getId();
        Intent intent = new Intent(getContext(), MyBroadcastReceiver.class);
        intent.putExtra("noteID", s);
        intent.setAction("LocationReminder");
        c.startActivity(intent);
    }

    private void addReminderToAlarmManager(DocumentSnapshot reminder) {
        Context c = getContext();
        TimeReminder timeReminder = reminder.toObject(TimeReminder.class);
        Log.i("timestamp string", Objects.requireNonNull(timeReminder).getTimestamp().toString());
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(c, MyBroadcastReceiver.class);
        myIntent.setAction("TimeReminder");
        myIntent.putExtra("whatsapp", false);
//                  for the noteID!

        DocumentReference d = reminder.getReference();
        CollectionReference coll = d.getParent();
        DocumentReference r = coll.getParent();
        String s = Objects.requireNonNull(r).getId();
        myIntent.putExtra("noteID", s);
//                    no need to check because it just sets the reminder again?
//            boolean isWorking = (PendingIntent.getBroadcast(c, 0, myIntent, PendingIntent.FLAG_NO_CREATE) != null);
//            if (!isWorking) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, myIntent, 0);
        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, Objects.requireNonNull(timeReminder).getTimestamp().toDate().getTime(), pendingIntent);
//            }

    }

    // handler listener
    @SuppressWarnings("FieldCanBeLocal")
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
                    forceStop.update("forceStop", true);
                    // here I do logging of exception to a db
                    PendingIntent myActivity = PendingIntent.getActivity(getContext(),
                            192837, new Intent(getContext(), MainActivity.class),
                            PendingIntent.FLAG_ONE_SHOT);
//                   trying to activate a service.
//                    Intent myIntent = new Intent(getContext(), MyBroadcastReceiver.class);
//                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, myIntent, 0);

                    AlarmManager alarmManager;
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Objects.requireNonNull(alarmManager).set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            15000, myActivity);
                    System.exit(2);

                    // re-throw critical exception further to the os (important)
                    uncaughtExceptionHandler.uncaughtException(thread, ex);
                }
            };

}