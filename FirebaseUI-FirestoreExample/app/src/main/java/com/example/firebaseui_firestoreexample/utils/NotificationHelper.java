package com.example.firebaseui_firestoreexample.utils;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import com.example.firebaseui_firestoreexample.MyApp;
import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.firestore_data.LocationReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.ReminderData;
import com.example.firebaseui_firestoreexample.firestore_data.TimeReminderData;
import com.example.firebaseui_firestoreexample.receivers.NotificationReceiver;
import com.example.firebaseui_firestoreexample.reminders.LocationReminder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

public class NotificationHelper {

    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    private String reminderID;
    private String noteID;

    public NotificationHelper(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in
     * the support library
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void initNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        for (NotificationChannel channel : NotificationChannels.getChannels()) {
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public void createNotification(String title,
                                   String content,
                                   PendingIntent notifyIntent,
                                   DocumentReference reminderDocumentReference) {


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        reminderID = reminderDocumentReference.getId();
        CollectionReference coll = reminderDocumentReference.getParent();
        DocumentReference documentReference = coll.getParent();
        if (documentReference != null) {
            noteID = documentReference.getId();
        }


        PendingIntent pendingIntentSwiped = createPendingIntent("swiped");

        String doneButton = mContext.getString(R.string.done);
        PendingIntent donePendingIntentButton = createPendingIntent("done");

        String trashButton = mContext.getString(R.string.trash);
        PendingIntent trashPendingIntentButton = createPendingIntent("trash");

        String snoozeButton = mContext.getString(R.string.snooze);
        PendingIntent snoozePendingIntentButton = createPendingIntent("snooze");

        String nextTimeButton = mContext.getString(R.string.next_time);
        PendingIntent nextTimePendingIntentButton = createPendingIntent("next time");

        String whatsappButton = mContext.getString(R.string.sendWhatsapp);
        PendingIntent sendWhatsappPendingIntentButton = createPendingIntent("send whatsapp");


        mBuilder = new NotificationCompat.Builder(mContext, "CHANNEL_ID");
        Builder builder = mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notifyIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .addAction(R.drawable.ic_save, trashButton, trashPendingIntentButton)
                .setDeleteIntent(pendingIntentSwiped);// this gets triggered when we dismiss (swipe away) the notification or click clear all in the notification panel.

        if (MyApp.timeReminders.containsKey(reminderDocumentReference.getId())) {
            builder.addAction(R.drawable.ic_save, doneButton, donePendingIntentButton);
            builder.addAction(R.drawable.ic_save, snoozeButton, snoozePendingIntentButton);
            TimeReminderData timeReminderData = MyApp.timeReminders.get(reminderID);
            if (isNetworkAvailable() && timeReminderData != null
                    && timeReminderData.getTimeReminder().getWhatsappNumber() != null
                        && !timeReminderData.getTimeReminder().getWhatsappNumber().equals(""))
                builder.addAction(R.drawable.ic_save, whatsappButton, sendWhatsappPendingIntentButton);
        }
        if (MyApp.locationReminders.containsKey(reminderDocumentReference.getId())) {
            LocationReminderData locationReminderData = MyApp.locationReminders.get(reminderID);
            if (locationReminderData != null) {
            LocationReminder locationReminder = locationReminderData.getLocationReminder();
            builder.addAction(R.drawable.ic_save, doneButton, donePendingIntentButton);
            builder.addAction(R.drawable.ic_save, nextTimeButton, nextTimePendingIntentButton);
            if (isNetworkAvailable()
                    && locationReminder.getWhatsappNumber() != null
                    && !locationReminder.getWhatsappNumber().equals(""))
                builder.addAction(R.drawable.ic_save, whatsappButton, sendWhatsappPendingIntentButton);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId("CHANNEL_ID");
        }
    }

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent(mContext, NotificationReceiver.class);
        intent.putExtra("reminderID", reminderID);
        intent.putExtra("noteID", noteID);
        intent.setAction(action);
        return PendingIntent.getBroadcast(mContext.getApplicationContext(), MyApp.createAlarmID(), intent, 0);
    }


    @SuppressWarnings("unused")
    public Builder getBuilder() {
        return mBuilder;
    }

    @SuppressWarnings("unused")
    public NotificationHelper setVibration(long[] pattern) {
        if (pattern == null || pattern.length == 0) {
            pattern = new long[]{500, 500};
        }
        mBuilder.setVibrate(pattern);
        return this;
    }

    public void show(int id) {
        mNotificationManager.notify(id, mBuilder.build());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null; //added to avoid warning
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }
}
