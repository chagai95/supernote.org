package com.example.firebaseui_firestoreexample;


import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.firebaseui_firestoreexample.activities.ShowErrorActivity;
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.firestore_data.LocationReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.OfflineNoteData;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.UserReminderData;
import com.example.firebaseui_firestoreexample.receivers.MyBroadcastReceiver;
import com.example.firebaseui_firestoreexample.reminders.LocationReminder;
import com.example.firebaseui_firestoreexample.reminders.Reminder;
import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.example.firebaseui_firestoreexample.reminders.UserReminder;
import com.example.firebaseui_firestoreexample.reminders.WhatsappTimeReminder;
import com.example.firebaseui_firestoreexample.utils.NetworkChangeReceiver;
import com.example.firebaseui_firestoreexample.utils.NetworkUtil;
import com.example.firebaseui_firestoreexample.utils.TrafficLight;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MyApp extends Application {
    public static boolean userSkippedLogin;
    public static CloudUserData myCloudUserData;
    public static String userUid;
    private static MyApp firstInstance;
    public static boolean updateFromServer;
    public static String titleOldVersion;// perhaps with an intent?
    public static String recyclerViewMode;
    public static String recyclerViewModeReminder;
    public static boolean recyclerViewModeReminderShowDone;
    public static boolean recyclerViewModeReminderShowOtherUsers;
    public static long totalTime;
    public static HashMap<String, OfflineNoteData> allNotes;
    public static HashMap<String, TimeReminderData> timeReminders;
    public static HashMap<String, LocationReminderData> locationReminders;
    public static HashMap<String, ReminderData> otherUserReminders;
    public static HashMap<String, CloudUserData> friends;
    public static HashMap<String, CloudUserData> userReminderUsers;
    private static boolean activityMainVisible;
    private static boolean activityLoginVisible;
    private static boolean activityVisible;
    private static boolean activitySettingsVisible;
    private static boolean activityEditNoteVisible;
    private static boolean backUpFailed;
    public static boolean appStarted;
    //    makeText(c, "might not be up to date last updated:", LENGTH_SHORT).show();
    FirebaseFirestore db;
    public static DocumentReference forceStop;
    public static boolean internetDisabledInternally;
    public static boolean autoInternInternetOffWhenSlow;
    public static boolean showDialogWhenInternetSlow;
    public static TrafficLight currentTrafficLightState;


    // uncaught exception handler variable
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;


    public MyApp() {
        initialize();
//        startAppOffline();
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    private static void initialize() {
        allNotes = new HashMap<>();
        totalTime = 0;
        backUpFailed = false;
        autoInternInternetOffWhenSlow = false;
        internetDisabledInternally = false;
        appStarted = false;
        showDialogWhenInternetSlow = true;
        timeReminders = new HashMap<>();
        locationReminders = new HashMap<>();
        otherUserReminders = new HashMap<>();
        friends = new HashMap<>();
        userReminderUsers = new HashMap<>();
        recyclerViewMode = "default";
        recyclerViewModeReminder = "default";
        recyclerViewModeReminderShowDone = false;
        recyclerViewModeReminderShowOtherUsers = false;
    }

    /*private void startAppOffline() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("utils").document("startAppOffline")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if ((boolean) Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getData()).get("startAppOffline")) {
                    db.disableNetwork();
                    MyApp.internetDisabledInternally = true;
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
        fireStore();
        setLocationListener();
    }

    public void fireStore() {
        db = FirebaseFirestore.getInstance();
//        long long_one_mb = 1048576L;
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
        //startAppOffline();
        forceStop = db.collection("utils").document("forceStop");

        login();

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
    }

    public static void login() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userUid = firebaseUser.getUid();
            userSkippedLogin = firebaseUser.isAnonymous();
            saveUserToMyApp(firebaseUser);
        }
    }

    static void saveUserToMyApp(FirebaseUser firebaseUser) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (!userSkippedLogin)
            firebaseFirestore.collection("users").document(firebaseUser.getUid())
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;
                    CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                    assert cloudUser != null;
                    MyApp.myCloudUserData = new CloudUserData(cloudUser, documentSnapshot.getReference());
                    firebaseFirestore.enableNetwork();
                    loadAllNotes(firebaseFirestore);
                    loadAllRemindersAndRegisterListeners(firebaseFirestore);
                    loadFriends(firebaseFirestore);
                }
            });

//        disable network only after we get all data, that's why we call each method only when the other finishes
        if (userSkippedLogin) {
            loadAllNotes(firebaseFirestore);
        }

    }

    private static void loadFriends(FirebaseFirestore firebaseFirestore) {
        for (String uid :
                myCloudUserData.getCloudUser().getFriends()) {
            firebaseFirestore.collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;
                    CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                    assert cloudUser != null;
                    friends.put(uid, new CloudUserData(cloudUser, documentSnapshot.getReference()));
                }
            });
        }
    }

    public static void logout() {
        for (TimeReminderData timeReminderData :
                timeReminders.values()) {
            timeReminderData.getDocumentReference().get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;
                    Map<String, Object> data = documentSnapshot.getData();
                    assert data != null;
                    Timestamp timestamp = (Timestamp) data.get("timestamp");
                    assert timestamp != null;
                    if (!(boolean) data.get("done") &&
                            !(boolean) data.get("trash") &&
                            new Date().before(timestamp.toDate()))
                        removeReminderFromAlarmManager(documentSnapshot);
                }
            });
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        initialize();
    }

    void addConnectivityListener() {

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // check conn
            int status = NetworkUtil.getConnectivityStatusString(getContext());
//           restart activity when
            addConnectivityListener();
        }, 5000);
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    // very bad idea because onstop gets called after onResume!!!
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityStopped() {
        activityVisible = false;
    }

    public static boolean isActivityLoginVisible() {
        return activityLoginVisible;
    }

    public static void activityLoginResumed() {
        activityLoginVisible = true;
    }

    public static void activityLoginStopped() {
        activityLoginVisible = false;
    }

    public static boolean isActivitySettingsVisible() {
        return activitySettingsVisible;
    }

    public static void activitySettingsResumed() {
        activitySettingsVisible = true;
    }

    public static void activitySettingsStopped() {
        activitySettingsVisible = false;
    }

    public static boolean isActivityMainVisible() {
        return activityMainVisible;
    }

    public static void activityMainResumed() {
        activityMainVisible = true;
    }

    public static void activityMainStopped() {
        activityMainVisible = false;
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
        FirebaseFirestore.getInstance().collection("notes").whereEqualTo("loadToCache", true)
                .get(Source.SERVER).addOnCompleteListener(task -> backUpFailed = !task.isSuccessful());
        FirebaseFirestore.getInstance().collectionGroup("Reminders").whereGreaterThanOrEqualTo("timestamp", new Timestamp(new Date()))
                .whereLessThanOrEqualTo("timestamp", new Timestamp(getUntilWhen()))
                .whereEqualTo("type", "time")
                .whereEqualTo("done", true)
                .whereEqualTo("trash", true)
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

    private static void loadAllNotes(FirebaseFirestore firebaseFirestore) {
        Query queryMyNotes = firebaseFirestore.collection("notes").whereEqualTo("creator",
                userUid);

        Source source = Source.DEFAULT;
        if (userSkippedLogin)
            source = Source.CACHE;

        queryMyNotes.get(source).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (userSkippedLogin) {
                    firebaseFirestore.enableNetwork(); // in case it already got disabled while waiting for the task to finish.
                    loadAllRemindersAndRegisterListeners(firebaseFirestore);
                }
                QuerySnapshot queryDocumentSnapshots = task.getResult();
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot documentSnapshot :
                        queryDocumentSnapshots) {
                    assert documentSnapshot != null;
                    Note note = documentSnapshot.toObject(Note.class);
                    assert note != null;
                    DocumentReference documentReference = documentSnapshot.getReference();

                    if (MyApp.allNotes.get(documentReference.getId()) == null)
                        MyApp.allNotes.put(documentReference.getId(), new OfflineNoteData(documentReference));

                    OfflineNoteData offlineNoteData = MyApp.allNotes.get(documentReference.getId());
                    assert offlineNoteData != null;
                    offlineNoteData.setNote(note);
                    if (note.isKeepOffline()) {
                        ListenerRegistration listenerRegistration = documentReference.addSnapshotListener((documentSnapshotForListener, e) -> {
                            if (e != null) {
                                System.err.println("Listen failed: " + e);
                            }
                            assert documentSnapshotForListener != null;
                            offlineNoteData.setNote(documentSnapshotForListener.toObject(Note.class));
                        });
                        offlineNoteData.setListenerRegistration(listenerRegistration);
                    }

                }
            }

        });

        if (!userSkippedLogin) {
            Query querySharedNotes = firebaseFirestore.collection("notes")
                    .whereArrayContains("shared", userUid);
            querySharedNotes.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot documentSnapshot :
                            queryDocumentSnapshots) {
                        assert documentSnapshot != null;
                        Note note = documentSnapshot.toObject(Note.class);
                        assert note != null;
                        DocumentReference documentReference = documentSnapshot.getReference();

                        if (MyApp.allNotes.get(documentReference.getId()) == null)
                            MyApp.allNotes.put(documentReference.getId(), new OfflineNoteData(documentReference));

                        OfflineNoteData offlineNoteData = MyApp.allNotes.get(documentReference.getId());
                        assert offlineNoteData != null;
                        offlineNoteData.setNote(note);
                        if (note.isKeepOffline()) {
                            ListenerRegistration listenerRegistration = documentReference.addSnapshotListener((documentSnapshotForListener, e) -> {
                                if (e != null) {
                                    System.err.println("Listen failed: " + e);
                                }
                                assert documentSnapshotForListener != null;
                                offlineNoteData.setNote(documentSnapshotForListener.toObject(Note.class));
                            });
                            offlineNoteData.setListenerRegistration(listenerRegistration);
                        }

                    }
                }

            });
        }

    }

    private static void loadAllRemindersAndRegisterListeners(FirebaseFirestore firebaseFirestore) {
//        delete all reminders from everywhere.
        /*firebaseFirestore.collectionGroup("Reminders") .get().addOnCompleteListener(task -> {
                   if(task.isSuccessful()){
                       for (DocumentSnapshot documentSnapshot :
                               task.getResult().getDocuments()        ) {
                           documentSnapshot.getReference().delete();
                       }

                   }

                });*/

        Query queryCreatedReminders = firebaseFirestore.collectionGroup("Reminders").whereEqualTo("uid",
                userUid);

//      the created reminders are the ones which need to get loaded when the user is not a cloud user because
//      the user can only notify himself.
        if (userSkippedLogin) {
            queryCreatedReminders.get(Source.CACHE).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    assert querySnapshot != null;
                    for (DocumentSnapshot documentSnapshot :
                            querySnapshot.getDocuments()) {
                        addReminderAndRegisterListener(documentSnapshot, firebaseFirestore);
                    }
                    firebaseFirestore.disableNetwork();
                }

            });


        }
        if (!userSkippedLogin) {
            queryCreatedReminders.addSnapshotListener((queryDocumentSnapshots, e) -> {

                if (internetDisabledInternally || userSkippedLogin)
                    firebaseFirestore.disableNetwork();
                if (e != null) {
                    return;
                }

                assert queryDocumentSnapshots != null;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    QueryDocumentSnapshot queryDocumentSnapshot = dc.getDocument();
                    if (timeReminders.get(queryDocumentSnapshot.getId()) == null
                            && locationReminders.get(queryDocumentSnapshot.getId()) == null)
                        switch (dc.getType()) {
                            case ADDED:
                                // might be a double in two of the maps - check first if it exists in the other map - only call this query
                                // as soon as the query with notified is done that we don't get doubles.
                                otherUserReminders.put(queryDocumentSnapshot.getId(),
                                        new ReminderData(queryDocumentSnapshot.getReference(), getReminder(queryDocumentSnapshot)));
                                break;
                            case MODIFIED:
                                break;
                            case REMOVED:
                                otherUserReminders.remove(queryDocumentSnapshot.getId());
                                break;
                        }
                }
            });

            Query queryNotifyingReminders = firebaseFirestore.collectionGroup("Reminders").whereArrayContains("notifyUsers",
                    userUid);

            queryNotifyingReminders.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (MyApp.internetDisabledInternally)
                    firebaseFirestore.disableNetwork();
                if (e != null) {
                    return;
                }

                assert queryDocumentSnapshots != null;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    QueryDocumentSnapshot queryDocumentSnapshot = dc.getDocument();
                    switch (dc.getType()) {
                        case ADDED:
                            addReminderAndRegisterListener(queryDocumentSnapshot, firebaseFirestore);
                            break;
                        case MODIFIED:
                            Map<String, Object> data = queryDocumentSnapshot.getData();
//                                might cause problems - removing an alarm which does not exist.
                            if ((boolean) data.get("done") || (boolean) data.get("trash")) {
                                String type = (String) data.get("type");
                                assert type != null;
                                if (type.equals("time") || type.equals("whatsapp time"))
                                    removeReminderFromAlarmManager(queryDocumentSnapshot);
                                if (type.equals("user")) {
                                    UserReminderData userReminderData = (UserReminderData) locationReminders.get(queryDocumentSnapshot.getId());
                                    assert userReminderData != null;
                                    userReminderData.setUserReminder(queryDocumentSnapshot.toObject(UserReminder.class));
                                }
                            }
                            if (!(boolean) data.get("done") && !(boolean) data.get("trash"))
                                addReminderAndRegisterListener(queryDocumentSnapshot, firebaseFirestore);
                            break;
                        case REMOVED:
                            removeReminder(dc.getDocument());
                            break;
                    }
                }
            });
        }


    }

    private static Reminder getReminder(QueryDocumentSnapshot queryDocumentSnapshot) {
        Reminder reminder = null;
        switch ((String) Objects.requireNonNull(queryDocumentSnapshot.getData().get("type"))) {
            case "time":
                reminder = queryDocumentSnapshot.toObject(TimeReminder.class);
                break;
            case "location":
                reminder = queryDocumentSnapshot.toObject(LocationReminder.class);
                break;
            case "user":
                reminder = queryDocumentSnapshot.toObject(UserReminder.class);
                break;
            case "whatsapp time":
                reminder = queryDocumentSnapshot.toObject(WhatsappTimeReminder.class);
                break;
        }
        return reminder;
    }

    static void addReminderAndRegisterListener(DocumentSnapshot reminder, FirebaseFirestore firebaseFirestore) {
        DocumentReference reminderDocRef = reminder.getReference();
        String type = (String) Objects.requireNonNull(reminder.getData()).get("type");
        assert type != null;
        switch (type) {
            case "time":
                TimeReminder timeReminder = reminder.toObject(TimeReminder.class);
                assert timeReminder != null;
                if (!timeReminder.getTimestamp().toDate().before(new Date()))
                    addReminderToAlarmManager(reminder);
                timeReminders.put(reminderDocRef.getId(),
                        new TimeReminderData(reminderDocRef, timeReminder));
                break;
            case "location":
                LocationReminder locationReminder = reminder.toObject(LocationReminder.class);
                locationReminders.put(reminderDocRef.getId(), new LocationReminderData(reminderDocRef, locationReminder));
                break;
            case "user":
                UserReminder userReminder = reminder.toObject(UserReminder.class);
                assert userReminder != null;
//                perhaps save the users and their listeners and delete them when they don't have any reminders left
//                with that user?
                DocumentReference documentReferenceUser = firebaseFirestore.collection("users").document(userReminder.getCloudUserID());
                documentReferenceUser.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        assert documentSnapshot != null;
                        CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                        assert cloudUser != null;
                        CloudUserData newUserCloudUserData = new CloudUserData(cloudUser, documentSnapshot.getReference());
                        userReminderUsers.put(documentReferenceUser.getId(), newUserCloudUserData);
                        friends.put(newUserCloudUserData.getCloudUser().getUid(), newUserCloudUserData);
                    }
                });

                documentReferenceUser.addSnapshotListener((documentSnapshot, e) -> {
                    assert documentSnapshot != null;
                    CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                    assert cloudUser != null;
                    GeoPoint geoPoint = cloudUser.getGeoPoint();
                    reminder.getReference().update("geoPoint", geoPoint);
                });
                locationReminders.put(reminderDocRef.getId(), new UserReminderData(reminderDocRef, userReminder));
                break;
            case "whatsapp time":
                WhatsappTimeReminder whatsappTimeReminder = reminder.toObject(WhatsappTimeReminder.class);
                assert whatsappTimeReminder != null;
                if (!whatsappTimeReminder.getTimestamp().toDate().before(new Date()))
                    addReminderToAlarmManager(reminder);
                timeReminders.put(reminderDocRef.getId(),
                        new TimeReminderData(reminderDocRef, whatsappTimeReminder));
                break;
        }
    }

    static void removeReminder(DocumentSnapshot reminder) {
        DocumentReference reminderDocRef = reminder.getReference();
        String type = (String) Objects.requireNonNull(reminder.getData()).get("type");
        assert type != null;
        switch (type) {
            case "time":
            case "whatsapp time":
                timeReminders.remove(reminderDocRef.getId());
                removeReminderFromAlarmManager(reminder);
                break;
            case "location":
            case "user":
                locationReminders.remove(reminderDocRef.getId());
                break;
        }
    }

    public static void setLocationListener() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (myCloudUserData != null)
                    myCloudUserData.getDocumentReference().update("geoPoint",
                            new GeoPoint(location.getLatitude(), location.getLongitude()));

                for (LocationReminderData locationData :
                        MyApp.locationReminders.values()) {
                    LocationReminder locationReminder = locationData.getLocationReminder();
                    if (!locationReminder.isDone() && !locationReminder.isTrash()) {
                        GeoPoint geoPoint = locationReminder.getGeoPoint();
                        if (geoPoint != null) {
                            Location locationLocationReminder = new Location("");//provider name is unnecessary
                            locationLocationReminder.setLatitude(geoPoint.getLatitude());
                            locationLocationReminder.setLongitude(geoPoint.getLongitude());
                            float distanceInMeters = locationLocationReminder.distanceTo(location);
                            if (locationReminder.getRadius() > (double) distanceInMeters) {
                                Toast.makeText(getContext(), location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                                createNotificationForLocationReminder(locationData.getDocumentReference(), getContext());
                            }
                        }
                    }
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
                getContext().startActivity(i);
            }
        };


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        Objects.requireNonNull(locationManager).requestLocationUpdates("gps", 2000, 8, listener);
    }

    public static void createNotificationForLocationReminder(DocumentReference reminderDocRef, Context c) {
        CollectionReference coll = reminderDocRef.getParent();
        DocumentReference documentReference = coll.getParent();
        assert documentReference != null;
        MyBroadcastReceiver.createNotification(c, documentReference, reminderDocRef);
    }

    public static void createNotificationForUserReminder(DocumentReference reminderDocRef, Context c) {
        CollectionReference coll = reminderDocRef.getParent();
        DocumentReference documentReference = coll.getParent();
        assert documentReference != null;
        MyBroadcastReceiver.createNotification(c, documentReference, reminderDocRef);
    }

    static void addReminderToAlarmManager(DocumentSnapshot reminder) {
        Context c = getContext();
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(c, MyBroadcastReceiver.class);

        Pair<Intent, TimeReminder> intentAndTimeReminderForAlarmManager = createIntentAndTimeReminderForAlarmManager(myIntent, reminder);
        assert intentAndTimeReminderForAlarmManager != null;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intentAndTimeReminderForAlarmManager.first, 0);
        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, intentAndTimeReminderForAlarmManager.second.getTimestamp().toDate().getTime(), pendingIntent);

    }

    static void removeReminderFromAlarmManager(DocumentSnapshot reminder) {
        Context c = getContext();
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(c, MyBroadcastReceiver.class);

        Pair<Intent, TimeReminder> intentAndTimeReminderForAlarmManager = createIntentAndTimeReminderForAlarmManager(myIntent, reminder);
        assert intentAndTimeReminderForAlarmManager != null;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intentAndTimeReminderForAlarmManager.first, 0);
        Objects.requireNonNull(alarmManager).cancel(pendingIntent);

    }

    static Pair<Intent, TimeReminder> createIntentAndTimeReminderForAlarmManager(Intent myIntent, DocumentSnapshot reminder) {
        myIntent.putExtra("reminderID", reminder.getId());
        Map<String, Object> data = reminder.getData();
        assert data != null;
        TimeReminder timeReminder = null;
        switch ((String) Objects.requireNonNull(data.get("type"))) {
            case "time":
                myIntent.setAction("TimeReminder");
                timeReminder = reminder.toObject(TimeReminder.class);
                break;
            case "whatsapp time":
                myIntent.setAction("WhatsappTimeReminder");
                timeReminder = reminder.toObject(WhatsappTimeReminder.class);
                assert timeReminder != null;
                myIntent.putExtra("whatsappNumber", ((WhatsappTimeReminder) timeReminder).getNumber());
                myIntent.putExtra("whatsappMessage", ((WhatsappTimeReminder) timeReminder).getMessage());
                break;
        }

//        for the noteID!
        DocumentReference d = reminder.getReference();
        CollectionReference coll = d.getParent();
        DocumentReference r = coll.getParent();
        String s = Objects.requireNonNull(r).getId();
        myIntent.putExtra("noteID", s);

        return Pair.create(myIntent, timeReminder);
    }


    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public synchronized static String getDeviceID(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
                registerNewDevice(uniqueID);
            }
        }
        return uniqueID;
    }

    private static void registerNewDevice(String uniqueID) {
        FirebaseFirestore.getInstance().collection("utils").document("devices")
                .update("id's", FieldValue.arrayUnion(uniqueID));
        FirebaseFirestore.getInstance().collection("utils").document("devices")
                .update("names", FieldValue.arrayUnion(uniqueID));
    }

    // handler listener
    @SuppressWarnings("FieldCanBeLocal")
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
                    // here I do logging of exception to a db
                    forceStop.update("forceStop", true);
                    Intent intent = new Intent(getContext(), ShowErrorActivity.class);
                    String[] stackTrace = new String[ex.getCause().getStackTrace().length];
                    for (int i = 0; i < ex.getCause().getStackTrace().length; i++) {
                        stackTrace[i] = ex.getCause().getStackTrace()[i].toString();
                    }
                    intent.putExtra("message", ex.getMessage());
                    intent.putExtra("stackTrace", stackTrace);

                    PendingIntent myActivity = PendingIntent.getActivity(getContext(),
                            192837, intent,
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