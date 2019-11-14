package com.example.firebaseui_firestoreexample;


import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.firebaseui_firestoreexample.activities.ShowErrorActivity;
import com.example.firebaseui_firestoreexample.firestore_data.CloudUserData;
import com.example.firebaseui_firestoreexample.firestore_data.LocationReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.NoteData;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.UserReminderData;
import com.example.firebaseui_firestoreexample.receivers.MyBroadcastReceiver;
import com.example.firebaseui_firestoreexample.receivers.NotificationReceiver;
import com.example.firebaseui_firestoreexample.reminders.LocationReminder;
import com.example.firebaseui_firestoreexample.reminders.Reminder;
import com.example.firebaseui_firestoreexample.reminders.TimeReminder;
import com.example.firebaseui_firestoreexample.reminders.UserReminder;
import com.example.firebaseui_firestoreexample.reminders.WhatsappTimeReminder;
import com.example.firebaseui_firestoreexample.receivers.NetworkChangeReceiver;
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
import com.google.type.DayOfWeek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MyApp extends Application {

    //    create all queries for the different modes here and add all of them to the allNotes map
    public static Query queryNotTrashedNotes;
    public static Query queryTrashedNotes;
    public static Query querySharedNotes;


    public static boolean userSkippedLogin;
    public static CloudUserData myCloudUserData;
    public static String userUid;
    public static ArrayList<String> offlineUsernamesToBeAddedToFriendsWhenOnline;
    public static ArrayList<String> offlineUsernamesToBeAddedToFriendsWhenOnlineNonExistingList;
    private static MyApp firstInstance;
    public static boolean updateFromServer;
    public static String descriptionOldVersion;// perhaps with an intent?
    public static String recyclerViewMode;
    public static String recyclerViewModeReminder;
    public static boolean recyclerViewModeReminderShowDone;
    public static boolean recyclerViewModeReminderShowOtherUsers;
    public static long totalTime;
    public static HashMap<String, NoteData> allNotes;
    public static HashMap<String, NoteData> emptyNotesForOfflineCreation;
    public static HashMap<String, TimeReminderData> timeReminders;
    public static HashMap<String, LocationReminderData> locationReminders;
    public static HashMap<String, ReminderData> otherUserReminders;
    public static HashMap<String, CloudUserData> friends;
    public static HashMap<String, CloudUserData> userReminderUsers; // possibly add this to UserReminderData
    private static boolean activityMainVisible;
    private static boolean activityLoginVisible;
    private static boolean activityVisible;
    private static boolean activitySettingsVisible;
    private static boolean activityEditNoteVisible;
    private static boolean backUpFailed;
    public static boolean appStarted;
    private static int alarmIDCounter;
    private static int notificationIDCounter;
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
        queryNotTrashedNotes = null;
        allNotes = new HashMap<>();
        emptyNotesForOfflineCreation = new HashMap<>();
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
        recyclerViewMode = "all";
        recyclerViewModeReminder = "all";
        recyclerViewModeReminderShowDone = false;
        recyclerViewModeReminderShowOtherUsers = false;
        notificationIDCounter = 1;
        offlineUsernamesToBeAddedToFriendsWhenOnline = new ArrayList<>();
        offlineUsernamesToBeAddedToFriendsWhenOnlineNonExistingList = new ArrayList<>();
        recyclerViewModeReminderShowDone = true;
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
            if (timeReminderData.getAlarmID() != -1)
                removeReminderFromAlarmManager(timeReminderData);
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

                    allNotes.put(documentReference.getId(), new NoteData(documentReference));

                    NoteData noteData = MyApp.allNotes.get(documentReference.getId());
                    assert noteData != null;
                    noteData.setNote(note);
                    if (note.isKeepOffline()) {
                        ListenerRegistration listenerRegistration = documentReference.addSnapshotListener((documentSnapshotForListener, e) -> {
                            if (e != null) {
                                System.err.println("Listen failed: " + e);
                            }
                            assert documentSnapshotForListener != null;
                            noteData.setNote(documentSnapshotForListener.toObject(Note.class));
                        });
                        noteData.setListenerRegistration(listenerRegistration);
                    }

                }
            }

        });

        //queryNotTrashedNotes = firebaseFirestore.collection("notes").whereEqualTo("creator", userUid).whereEqualTo("trash", false);
        queryTrashedNotes = firebaseFirestore.collection("notes").whereEqualTo("creator", userUid).whereEqualTo("trash", true);

        if (!userSkippedLogin) {
            querySharedNotes = firebaseFirestore.collection("notes")
                    .whereArrayContains("shared", userUid);
            querySharedNotes.addSnapshotListener((queryDocumentSnapshots, e) -> {

                if (e != null) {
                    return;
                }

                assert queryDocumentSnapshots != null;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    QueryDocumentSnapshot queryDocumentSnapshot = dc.getDocument();
                    Note note = queryDocumentSnapshot.toObject(Note.class);
                    DocumentReference documentReference = queryDocumentSnapshot.getReference();

                    if (MyApp.allNotes.get(documentReference.getId()) == null)
                        MyApp.allNotes.put(documentReference.getId(), new NoteData(documentReference));

                    NoteData noteData = MyApp.allNotes.get(documentReference.getId());
                    assert noteData != null;
                    switch (dc.getType()) {
                        case ADDED:
                        case MODIFIED:
                            noteData.setNote(note);
                            break;
                        case REMOVED:
                            MyApp.allNotes.remove(documentReference.getId());
                            break;
                    }
                }
            });
        }


        Query queryEmptyNotes = firebaseFirestore.collection("notes").whereEqualTo("creator",
                "empty");

        queryEmptyNotes.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot queryDocumentSnapshots = task.getResult();
                assert queryDocumentSnapshots != null;
                if (queryDocumentSnapshots.size() < 50)
                    if (queryDocumentSnapshots.size() < 50)
                        createEmptyNotes();
                for (DocumentSnapshot documentSnapshot :
                        queryDocumentSnapshots) {
                    assert documentSnapshot != null;
                    Note note = documentSnapshot.toObject(Note.class);
                    assert note != null;
                    DocumentReference documentReference = documentSnapshot.getReference();
                    MyApp.emptyNotesForOfflineCreation.put(documentReference.getId(), new NoteData(documentReference));
                }
            }
        });


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

            new Thread(() -> {
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
                                ReminderData reminderData;
                                if (MyApp.timeReminders.containsKey(queryDocumentSnapshot.getId())) {
                                    reminderData = MyApp.timeReminders.get(queryDocumentSnapshot.getId());
                                    if (reminderData != null) {
                                        TimeReminder timeReminder = queryDocumentSnapshot.toObject(TimeReminder.class);
                                        reminderData.getReminder().setDone(timeReminder.isDone());
                                        reminderData.getReminder().setTrash(timeReminder.isTrash());
                                    }
                                }
                                if (MyApp.locationReminders.containsKey(queryDocumentSnapshot.getId())) {
                                    reminderData = MyApp.locationReminders.get(queryDocumentSnapshot.getId());
                                    if (reminderData != null) {
                                        reminderData.setReminder(queryDocumentSnapshot.toObject(LocationReminder.class));
                                    }
                                }


                                Map<String, Object> data = queryDocumentSnapshot.getData();
//                                might cause problems - removing an alarm which does not exist.
                                if ((boolean) data.get("done") || (boolean) data.get("trash")) {
                                    String type = (String) data.get("type");
                                    assert type != null;
                                    if (type.equals("time") || type.equals("whatsapp time")) {
                                        TimeReminderData timeReminderData = timeReminders.get(queryDocumentSnapshot.getId());
                                        assert timeReminderData != null;
                                        if (timeReminderData.getNotificationID() != 0)
                                            clearNotification(getContext(), timeReminderData.getNotificationID());
                                        if (timeReminderData.getAlarmID() != -1)
                                            removeReminderFromAlarmManager(timeReminderData);
                                    }

// no idea what I did here. maybe trying to update the location but that is actually already in the users maps listener.
                                    /*if (type.equals("user")) {
                                        UserReminderData userReminderData = (UserReminderData) locationReminders.get(queryDocumentSnapshot.getId());
                                        assert userReminderData != null;
                                        userReminderData.setUserReminder(queryDocumentSnapshot.toObject(UserReminder.class));
                                    }*/
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
            }).start();
        }


    }

    private static void clearNotification(Context context, int notificationID) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
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
        String reminderId = reminderDocRef.getId();
        assert type != null;
        switch (type) {
            case "time":
                TimeReminder timeReminder = reminder.toObject(TimeReminder.class);
                assert timeReminder != null;
                TimeReminderData timeReminderData = new TimeReminderData(reminderDocRef, timeReminder, -1);
                if (!timeReminder.getTimestamp().toDate().before(new Date())) {
                    addReminderToAlarmManager(timeReminderData);
                }
                if (timeReminders.get(reminderId) == null)
                    timeReminders.put(reminderId, timeReminderData);
                else
                    Objects.requireNonNull(timeReminders.get(reminderId)).setTimeReminder(timeReminder);
                break;
            case "location":
                LocationReminder locationReminder = reminder.toObject(LocationReminder.class);
                if (locationReminders.get(reminderId) == null)
                    locationReminders.put(reminderDocRef.getId(), new LocationReminderData(reminderDocRef, locationReminder));
                else
                    Objects.requireNonNull(locationReminders.get(reminderId)).setLocationReminder(locationReminder);
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
                LocationReminderData locationReminderData;
                if (locationReminders.get(reminderId) == null)
                    locationReminderData = locationReminders.put(reminderDocRef.getId(), new LocationReminderData(reminderDocRef, userReminder));
                else {
                    locationReminderData = Objects.requireNonNull(locationReminders.get(reminderId));
                    locationReminderData.setLocationReminder(userReminder);
                }

                documentReferenceUser.addSnapshotListener((documentSnapshot, e) -> {
                    assert documentSnapshot != null;
                    CloudUser cloudUser = documentSnapshot.toObject(CloudUser.class);
                    assert cloudUser != null;
                    GeoPoint geoPoint = cloudUser.getGeoPoint();
                    reminder.getReference().update("geoPoint", geoPoint);
                    if (locationReminderData != null)
                        if (!userReminder.isDone() && !userReminder.isTrash()) {
                            if (geoPoint != null) {
                                Location locationLocationReminder = new Location("");//provider name is unnecessary
                                locationLocationReminder.setLatitude(geoPoint.getLatitude());
                                locationLocationReminder.setLongitude(geoPoint.getLongitude());
                                LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
                                try {
                                    Location location = locationManager.getLastKnownLocation("gps");
                                    float distanceInMeters = locationLocationReminder.distanceTo(location);
                                    Calendar calendar = Calendar.getInstance();
                                    String today = MyDayOfWeek.values()[calendar.get(Calendar.DAY_OF_WEEK)].name().toLowerCase();
                                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                    if (userReminder.getNotifyUsers().contains(userUid))
                                        if (userReminder.getDaysOfWeek().contains(today) && userReminder.getStartHourOfDay() <= hour
                                                && userReminder.getEndHourOfDay() >= hour + 1)
                                            if (userReminder.getRadius() > (double) distanceInMeters) {
                                                if (!locationReminderData.isInRadius()) {
                                                    locationReminderData.setInRadius(true);
                                                    if (userReminder.isArrive())
                                                        createNotificationForLocationReminder(locationReminderData.getDocumentReference(), getContext());
                                                }
                                            } else if (locationReminderData.isInRadius()) {
                                                locationReminderData.setInRadius(false);
                                                if (userReminder.isLeave())
                                                    createNotificationForLocationReminder(locationReminderData.getDocumentReference(), getContext());
                                            }
                                } catch (SecurityException se) {
                                        Log.i("SecurityException", se.getMessage());
                                }
                            }
                        }
                });

                break;
        }
    }

    public static int createAlarmID() {
        alarmIDCounter++;
        return alarmIDCounter;
    }

    public static int createNotificationID() {
        notificationIDCounter++;
        return notificationIDCounter;
    }


    static void removeReminder(DocumentSnapshot reminder) {
        String type = (String) Objects.requireNonNull(reminder.getData()).get("type");
        assert type != null;
        switch (type) {
            case "time":
                TimeReminderData timeReminderData = timeReminders.remove(reminder.getId());
                assert timeReminderData != null;
                if (timeReminderData.getNotificationID() != 0)
                    clearNotification(getContext(), timeReminderData.getNotificationID());
                if (timeReminderData.getAlarmID() != -1)
                    removeReminderFromAlarmManager(timeReminderData);
                break;
            case "location":
            case "user":
                LocationReminderData locationReminderData = locationReminders.remove(reminder.getId());
                if (locationReminderData != null && locationReminderData.getNotificationID() != 0)
                    clearNotification(getContext(), locationReminderData.getNotificationID());
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
                            Calendar calendar = Calendar.getInstance();
                            String today = MyDayOfWeek.values()[calendar.get(Calendar.DAY_OF_WEEK)].name().toLowerCase();
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            if (locationReminder.getNotifyUsers().contains(userUid))
                                if (locationReminder.getDaysOfWeek().contains(today) && locationReminder.getStartHourOfDay() <= hour
                                        && locationReminder.getEndHourOfDay() >= hour + 1)
                                    if (locationReminder.getRadius() > (double) distanceInMeters) {
                                        if (!locationData.isInRadius()) {
                                            locationData.setInRadius(true);
                                            if (locationReminder.isArrive())
                                                createNotificationForLocationReminder(locationData.getDocumentReference(), getContext());
                                        }
                                    } else if (locationData.isInRadius()) {
                                        locationData.setInRadius(false);
                                        if (locationReminder.isLeave())
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
        Objects.requireNonNull(locationManager).requestLocationUpdates("gps", 10000, 15, listener);
    }

    public static void createNotificationForLocationReminder(DocumentReference reminderDocRef, Context c) {
        CollectionReference coll = reminderDocRef.getParent();
        DocumentReference documentReference = coll.getParent();
        assert documentReference != null;
        MyBroadcastReceiver.createNotification(c, documentReference.getId(), reminderDocRef);
    }

    static void addReminderToAlarmManager(TimeReminderData timeReminderData) {
        Context c = getContext();
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(c, MyBroadcastReceiver.class);

        Pair<Intent, TimeReminder> intentAndTimeReminderForAlarmManager = createIntentAndTimeReminderForAlarmManager(myIntent, timeReminderData);
        assert intentAndTimeReminderForAlarmManager != null;

        timeReminderData.setAlarmID(createAlarmID());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, timeReminderData.getAlarmID(), intentAndTimeReminderForAlarmManager.first, 0);
        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, intentAndTimeReminderForAlarmManager.second.getTimestamp().toDate().getTime(), pendingIntent);
    }

    static void removeReminderFromAlarmManager(TimeReminderData timeReminderData) {
        Context c = getContext();
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(c, MyBroadcastReceiver.class);

        Pair<Intent, TimeReminder> intentAndTimeReminderForAlarmManager = createIntentAndTimeReminderForAlarmManager(myIntent, timeReminderData);
        assert intentAndTimeReminderForAlarmManager != null;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, timeReminderData.getAlarmID(), intentAndTimeReminderForAlarmManager.first, 0);
        Objects.requireNonNull(alarmManager).cancel(pendingIntent);
    }

    static Pair<Intent, TimeReminder> createIntentAndTimeReminderForAlarmManager(Intent myIntent, TimeReminderData timeReminderData) {
        myIntent.putExtra("reminderID", timeReminderData.getDocumentReference().getId());
        TimeReminder timeReminder = timeReminderData.getTimeReminder();
        myIntent.setAction("reminder");

//        for the noteID!
        DocumentReference d = timeReminderData.getDocumentReference();
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

    public static void createEmptyNotes() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        // change to 100.
        for (int i = 0; i < 20; i++) {
            Note emptyNote = new Note("", "", null, "empty");
            firebaseFirestore.collection("notes").add(emptyNote)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = task.getResult();
                            assert documentReference != null;
                            emptyNotesForOfflineCreation.put(documentReference.getId(),
                                    new NoteData(documentReference));
                        }
                    });
        }
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